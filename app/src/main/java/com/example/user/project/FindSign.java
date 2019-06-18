package com.example.user.project;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class FindSign {

    enum DIR {
        BACKLEFT(0), BACKDOWN(1), BACKUP(2), PALMUP(3), BLADE(4), NOTFOUND(99);
        private int value;
        DIR(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

        public static DIR valueof(String index){
            if(index.equals("0")) return BACKLEFT;
            else if(index.equals("1")) return BACKDOWN;
            else if(index.equals("2")) return BACKUP;
            else if(index.equals("3")) return PALMUP;
            else if(index.equals("4")) return BLADE;

            return NOTFOUND;
        }
    };

    //0(자음 ㄱㄴㄷㄹ), 1(모음 ㅏㅑㅓㅕ)
    enum WORD{
        JAEM(0), MOEM(1), NOTFOUND(99);
        private int value;
        WORD(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public static WORD valueof(String index) {
            if (index.equals("0")) return JAEM;
            else if(index.equals("1")) return MOEM;
            else return NOTFOUND;
        }
    }

    enum CODE{
        TRANS(1), GAME(9);
        private int value;
        CODE(int x){value = x;}
    }

    private Handler handler;                   //메인스레드로 작업을 넘길 핸들러
    private String TAG = "compare";
    private BluetoothService btService = null;

    //-----------kmeans----------
    private Kmeans kmeans;
    private String[] inputFingerDatas;         //kmeans전 손가락 데이터
    private String[] outputFingerDatas;        //kmeans로 가공된 손가락 데이터
    //---------------------------

    private String[] DirData;                  //0번째 데이터: 안정화 여부(0:안정화 중, 1: 안정화 완료), 1번째 데이터 : 방향

    //------결과가 저장된 변수-----
    private String RESULT_SIGN = "";
    private String[] result;                 //굽힘(0~4번 인덱스), 방향(5번 인덱스) 번역 결과
    private int[] data;                   //화면에 출력될 데이터
    private String signResult;               //번역된 결과
    private String accSignResult;            //누적된 번역 결과
    //----------------------------

    //---DB에 접근 하는 핸들러, 테이블 명---
    private  InputDataSQLiteOpenHandler dbHandler;
    private  String tableName;
    private Vector<Vector<String>> signData;   //디비에서 얻어온 수화데이터
    //-----------------------------------

    private Context context;              //이 클래스를 호출할 액티비티의 Context
    private boolean searchFlag;           //의미를 찾을 스레드의 플래그
    private boolean bluetoothFlag;        //블루투스로 받아온 데이터를 필터링하는 스레드의 플래그

    //지화에 해당하는지 확인
    // code 1 : 번역모드, code 9 : 게임모드
    private static CODE code;

    private  MakeMeans makeMeans;
    WORD pastWord;              // 모음이 두번 연속으로 나오는지 확인하는 변수, 이전에 찾은 단어가 모음인지 자음인지 기억함
    String pastMean;

    private FindSign(Context context){
        this.context = context;

        if(btService == null)
            btService = BluetoothService.getInstance();
    }

    private static class LazyHolder{
        static final FindSign INSTANCE = new FindSign(MainActivity.getsApplication());
    }

    public static FindSign getInstance(CODE x){
        code = x;
        return LazyHolder.INSTANCE;
    }


    private void init(){
        tableName = "sign";
        dbHandler = new InputDataSQLiteOpenHandler(context, tableName);     //db핸들러 초기화
        DirData = new String[2];               //0번째 데이터: 안정화 여부(0:안정화 중, 1: 안정화 완료), 1번째 데이터 : 방향
        inputFingerDatas = new String[5];
        outputFingerDatas = new String[5];
        kmeans = new Kmeans(context);
        handler = new Handler();
        result = new String[6];           //굽힘, 방향 번역 결과
        data = new int[6];           //굽힘, 방향 데이터
        accSignResult = "";

        searchFlag = true;
        bluetoothFlag = true;
        makeMeans = new MakeMeans();
        pastWord = WORD.NOTFOUND;       //이전에 찾은 문자가 자음인지 모음인지 저장
        pastMean = "";                  //이전에 찾은 문자의 의미 저장

        //----------------------수화 데이터 불러옴------------------
        String[] coulmName = {"_id", "mean", "ofinger", "twfinger", "thfinger", "fofinger", "fifinger", "dir", "word"};
        signData = new Vector<Vector<String>>();

        for (int i = 0; i < coulmName.length; i++) {
            signData.add(dbHandler.select(coulmName[i]));
        }

        //---------------------------------------------------------

        for (int i = 0; i < inputFingerDatas.length; i++) {
            inputFingerDatas[i] = "-1";
            outputFingerDatas[i] = "-1";
        }
    }


    private synchronized void catchBluetoothdata(){       //블루투스로 데이터 받아옴
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d("thread", "catchBluetoothdata() 실행");
                while (bluetoothFlag) {   //====블루투스 데이터를 사용할 변수에 나눠서 저장하기====
                        try {
                            if(!btService.isRecive)
                                continue;

                            String[] inputBluetoothData = btService.getResult();

                            //----전송된 데이터 삽입부----
                            for (int i = 0; i < inputFingerDatas.length; i++)    //블루투스로 받아온 손가락 데이터 (5개)
                            {
                                inputFingerDatas[i] = inputBluetoothData[i];
                            }

                            DirData[0] = inputBluetoothData[5];     //블루투스로 받아온 안정화 여부
                            DirData[1] = inputBluetoothData[6];     //블루투스로 받아온 방향

                            for (int i = 0; i < 5; i++) {
                                data[i] = Integer.parseInt(inputFingerDatas[i]);
                            }
                            //---------------------------
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }
        });
        thread.start();
    }

    public void run(){
        SearchSign search = new SearchSign();

        init();                     //사용할 데이터 초기화
        catchBluetoothdata();       //블루투스로 받아온 데이터 분리
        kmeans.run();
        search.run();       //검색 시작
    }

    public void stop(){
        searchFlag = false;     //의미 찾기 종료
        bluetoothFlag = false;  //데이터 정제 종료
    }

    public String getAccSignResult(){ return accSignResult;}        //누적결과
    public int[] getData(){return data;}                            //숫자로 된 데이터
    public String getSignResult(){ return signResult;}              //문자 하나 번역 결과
    public String[] getResult(){ return result;}                    //문자로 표현된 손가락 방향

    public boolean getStable(){ return (DirData[0].equals("0"))?false:true;}
    public void setAccSignResult(String SccSignResult){
        this.accSignResult = SccSignResult;
    }

    public void callDeleteWord(){
        makeMeans.DeleteWord();
    }

    public class SearchSign{            //의미를 찾는 클래스

        public void run() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run(){
                    long pastTime = System.currentTimeMillis();         //이전 시간
                    String[] pastActivity = new String[6];              //이전 확정 결과

                    checkFinger();      //손가락 상태 확정
                    checkDir();         //방향 상태 확정

                    System.arraycopy(result, 0, pastActivity, 0, result.length);

                    Log.d("thread", "run() 실행");

                    while(searchFlag){
                        try{
                            checkFinger();      //손가락 상태 확정
                            checkDir();         //방향 상태 확정

                            boolean isSameActivity = true;

                            for(int i = 0; i < result.length; i++){         //동작이 같은지 확인함
                                if(!result[i].equals(pastActivity[i])){     //이전 동작과 다른 동작일 경우
                                    isSameActivity = false;
                                    Log.d(TAG, "다른 동작    " + i + "  번째 동작 "+ result[i] + "     " + pastActivity[i]);
                                    pastTime = System.currentTimeMillis();
                                    System.arraycopy(result, 0, pastActivity, 0, result.length);
                                    break;
                                }
                            }

                            if(isSameActivity) {         //이전과 동작이 같을 경우
                                if (System.currentTimeMillis() > pastTime + 500) {
                                    findSign();         //지화인지 확인
                                    pastTime = System.currentTimeMillis();
                                    System.arraycopy(result, 0, pastActivity, 0, result.length);
                                }
                            }

                            Thread.sleep(100);

                         }catch(Exception e){
                             e.printStackTrace();
                         }
                    }
                    Log.d("thread", "run() 종료");
                }
            });


            thread.start();
        }

        private void checkFinger(){      //손가락 상태 확정
            outputFingerDatas = kmeans.getResult(inputFingerDatas);

            String x;

            for(int i = 0; i < 5; i++) {                //손 굽힘정도 확인
                if ((x = outputFingerDatas[i]).equals("0"))
                    result[i] = "굽힘";
                else if (x.equals("1"))
                    result[i] = "펼침";
                else if (x.equals("-1"))
                    result[i] = "error";
            }
        }

        private void checkDir(){     //방향체크

            switch (DIR.valueof(DirData[1]))
            {
                case BACKLEFT:
                    result[5] = "손등왼쪽";
                    data[5] = 0;
                    break;
                case BACKDOWN:
                    result[5] = "손등아래";
                    data[5] = 1;
                    break;
                case BACKUP:
                    result[5] = "손등위";
                    data[5] = 2;
                    break;
                case PALMUP:
                    result[5] = "손바닥위";
                    data[5] = 3;
                    break;
                case BLADE:
                    result[5] = "측면";
                    data[5] = 4;
                    break;
                case NOTFOUND:
                    result[5] = "방향 없음";
                    data[5] = 99;
                    break;

            }

        }

        private synchronized void findSign() {
            Log.v(TAG, "findSign() 호출");

            boolean isSame = true;      //현재 상태와 디비의 내용이 일치하지 않을 경우 false로 변경. 다음 디비 내용과 비교

            for (int k = 0; k < signData.elementAt(0).size(); k++) {

                //------------손가락 굽힘으로 비교----------
                Log.v(TAG, k + "번째 수화 비교, 의미 : " +  signData.elementAt(1).elementAt(k));
                for (int j = 0; j < 5; j++) {        //손가락 데이터
                    if (DirData[1].equals(signData.elementAt(7).elementAt(k))) {     //방향비교
                        Log.v(TAG, "exfinger : " + outputFingerDatas[j] + "  ,  vector : " + signData.elementAt(j + 2).elementAt(k));
                        if (!outputFingerDatas[j].equals(signData.elementAt(j + 2).elementAt(k))) {     //손가락 데이터와 수화데이터 비교
                            isSame = false;         //손가락 데이터와 수화데이터가 다를 경우 비교를 종료하고 다음 수화를 비교함.
                            Log.v(TAG, k + "번째 손가락 비교 실패");
                            break;
                        }
                    } else {
                        isSame = false;
                        Log.v(TAG, k + "번째 수화 방향 비교 실패");
                        break;
                    }
                }

                //----------------------------------------
                //word : 0(자음 ㄱㄴㄷㄹ), 1(모음 ㅏㅑㅓㅕ)
                if (isSame) {           //결과 찾음
                    final int SignIdex = k;
                    boolean isWord = true;      //자음 다음 자음이 올 경우 또는 모음 다음 모음이 올 경우 false

                    Log.d(TAG, "------찾았다!!----");
                    Log.d(TAG, Thread.currentThread()+"");
                    Log.d(TAG, "pastWord : " + pastWord);
                    Log.d(TAG, "들어온 단어 : " + signData.elementAt(1).elementAt(k) + "");

                    if(code == CODE.valueOf("TRANS")) {         //번역모드일 때만 구분
                        //----------------자음 다음 자음, 모음 다음 모음이 나올 경우 결과를 확정하지 않음----------------
                        if (((pastWord == WORD.MOEM) && (WORD.valueof(signData.elementAt(8).elementAt(SignIdex)) == WORD.MOEM))) {    //이전 결과가 모음이고 현재 결과도 모음일 경우 결과 확정 안함
                            isWord = false;
                        }

                        //------------------------------같은 자음은 연속으로 나오지 않음--------------------------------
                        if (pastMean.equals(signData.elementAt(1).elementAt(SignIdex))) {
                            isWord = false;
                        }
                    }

                    Log.d(TAG, isWord + "");
                    if(isWord){         //자음 다음 모음 또는 모음 다음 자음이 검출됨

                        signResult = signData.elementAt(1).elementAt(SignIdex);          //의미 검출
                        pastMean = signResult;
                        accSignResult = makeMeans.findmeans(signResult.charAt(0));      //문자 조합, 결과 누적

                        //Log.d(TAG, "signData valueof : " + WORD.valueof(signData.elementAt(8).elementAt(SignIdex) + ""));

                        //이전 문자가 자음인지 모음인지 기록
                        pastWord = WORD.valueof(signData.elementAt(8).elementAt(SignIdex) + "");

                    }
                    break;

                } else {
                    isSame = true;
                    RESULT_SIGN = "";

                    signResult = "결과 없음";
                }

                Log.v(TAG, "signResult 결과 : " + signResult);

            }
        }
    }

    public class MakeMeans {

        // 초성 중성 종성 배열 순서대로 함.(유니코드 순서)
        private  final char[] CHO = { 'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ',
                'ㅌ', 'ㅍ', 'ㅎ' };
        private  final char[] JUN = { 'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ',
                'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ' };
        private  final char[] JON = { 'X', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ',
                'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };

        private  List<Integer> ChoList; // 초성 저장
        private  List<Integer> JunList; // 중성 저장
        private  List<Integer> JonList; // 종성 저장

        private  int index = -1;
        private  boolean isCho = false;
        private  boolean isJun = false; // 전 글자가 중성이였나? 초기는 true로 해줘야 시작때 모음을 안 받음
        private  boolean isJon = false; // 전 글자가 종성이였나?
        // 초성 찾기

        MakeMeans() {
            ChoList = new ArrayList<Integer>(); // 초성 저장
            JunList = new ArrayList<Integer>(); // 중성 저장
            JonList = new ArrayList<Integer>(); // 종성 저장
        }

        int CheckCho(char input) {
            for (int i = 0; i < CHO.length; i++) {
                if (input == CHO[i])
                    return i;
            }
            return -1;
        }

        // 중성 찾기
        int CheckJun(char input) {
            for (int i = 0; i < JUN.length; i++) {
                if (input == JUN[i])
                    return i;
            }
            return -1;
        }

        // 종성 찾기
        int CheckJon(char input) {
            for (int i = 0; i < JON.length; i++) {
                if (input == JON[i])
                    return i;
            }
            return 0;
        }

        // 출력
        String _Print() {
            int a = 0, b = 0, c = 0;
            char temp;
            String lastStr = "";

            for (int i = 0; i < ChoList.size(); i++) { // 초성으로 시작
                c = 0; // 종성이 0이면 없는 것이므로 초기화는 꼭 해야함!
                a = ChoList.get(i);
                if (JunList.size() != i) { // 중성이 있나 확인
                    b = JunList.get(i);
                    if (JonList.size() != i) // 종성이 있나 확인
                        c = JonList.get(i);
                    temp = (char) (0xAC00 + 28 * 21 * (a) + 28 * (b) + (c)); // 유니코드로 조합하는 공식
                } else {
                    temp = CHO[ChoList.get(i)]; // 중성이 없으면 초성에서 찾아야함(안 찾으면 기본 글자 ㅏ 가 추가됨)
                }
                lastStr = lastStr.concat(String.valueOf(temp));
            }
            return lastStr;
        }

        public String findmeans(char input) {
            char _inputChar; // 입력 받은 char형 저장
            _inputChar = input; // 키 입력 받기
            if (CheckJun(_inputChar) != -1 && (isCho || isJon)) { // 종성부터 검사(초성과 종성때문에 어쩔 수 없음)
                isJun = true; // 중성이였다고 확인
                if (isJon) { // 전 글자가 종성이였으면
                    isJon = false; // 종성 취소하고
                    isCho = true;
                    index++;
                    ChoList.add(index, CheckCho(JON[JonList.get(index - 1)])); // 종성을 초성으로 변환
                    JonList.set(index - 1, 0); // 종성은 0으로 초기화
                    JunList.add(index, CheckJun(_inputChar)); // 중성 입력

                    String str = _Print(); // 출력
                    return str;

                } else if (isCho) { // 초성이면
                    if (JunList.size() != index + 1)
                        JunList.add(index, CheckJun(_inputChar)); // 초성으로 추가
                    else
                        JunList.set(index, CheckJun(_inputChar));

                    String str = _Print(); // 출력
                    return str;
                }
            }

            if (isJun) { // 중성이 이였으면
                if (CheckJon(_inputChar) != 0) { // 종성에 해당되나 확인
                    JonList.add(index, CheckJon(_inputChar)); // 종성으로 추가
                    isCho = false;
                    isJun = false; // 중성 취소
                    isJon = true; // 종성이였다고 확인

                    String str = _Print(); // 출력
                    return str;

                } else {
                    if (CheckCho(_inputChar) != -1) { // 초성에 해당되는지 확인
                        index++; // 초성이므로 다음 글자에서 시작
                        isCho = true;
                        isJun = false; // 중성 취소
                        isJon = false; // 종성 취소
                        if (ChoList.size() != index + 1)
                            ChoList.add(index, CheckCho(_inputChar)); // 초성으로 추가
                        else
                            ChoList.set(index, CheckCho(_inputChar));

                        String str = _Print(); // 출력
                        return str;
                    }
                }
            } else if (!isCho) {
                if (CheckCho(_inputChar) != -1) { // 초성에 해당되는지 확인
                    index++;
                    isCho = true;
                    isJon = false; // 종성 취소
                    isJun = false; // 중성 취소
                    if (ChoList.size() != index + 1)
                        ChoList.add(index, CheckCho(_inputChar)); // 초성으로 추가
                    else
                        ChoList.set(index, CheckCho(_inputChar));

                    String str = _Print(); // 출력
                    return str;
                }
            }
            return "";
        }


        public  void DeleteWord() {
            if (isJon) {
                if (index != -1) {
                    JonList.set(index, 0);
                    isJon = false;
                    isJun = true;
                    isCho = true;
                }
                accSignResult = _Print();
            }
            if (isJun) {
                if (index != -1) {
                    JunList.remove(index);
                    isJun = false;
                    isJon = false;
                    isCho = true;
                }
                accSignResult = _Print();
            }
            if (isCho) {
                ChoList.remove(index);
                index--;
                isCho = false;
                if (index != 0 && index != -1) {
                    if (JonList.get(index) != 'X') {
                        isJun = true;
                        isJon = false;
                    } else {
                        isJon = true;
                    }
                } else {
                    if (index != -1) {
                        isJun = true;
                        isJon = false;
                    } else {
                        isJun = false;
                        isJon = false;
                    }
                }
                accSignResult = _Print();
            }
        }
    }

}

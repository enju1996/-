package com.example.user.project;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Vector;

//구부림, 펼침 두가지 상태
enum STATE {
    BLOW(0), SPREED(1);
    private int value;
    STATE(int value) {
        this.value = value;
    }
    public int getValue() { return value; }
}

//엄지~새끼
enum FINGER {
    ON(0), TW(1), TH(2), FO(3), FI(4);
    private int value;
    FINGER(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}

public class Kmeans extends AppCompatActivity{
    private State[] state;
    private  InputDataSQLiteOpenHandler[] handler;
    private int[][] center;
    private Context context;
    private String TAG = "Kmeans";

    //데이터베이스로부터 가져온 하나의 손가락 데이터
    //                  굽힘                                                 펼침
    //                   ㅣ                                                  ㅣ
    //            ---------------------------                      --------------------------
    //           ㅣ       ㅣ   ㅣ    ㅣ    ㅣ                      ㅣ        ㅣ   ㅣ   ㅣ   ㅣ
    //      엄지 데이터   검지  중지  약지  새끼                 엄지 데이터   검지  중지  약지  새끼

    class Finger{
        //상태별 데이터
        private  Vector<String> data;            //디비로부터 가져온 손가락 하나 데이터
        private String columnName;               //테이블에 있는 손가락 하나의 컬럼이름

        Finger(String columnName){
            data = new Vector<String>(5);
            this.columnName = columnName;
        }
    }

    //상태별로(폈을때와 굽혔을 때) 손가락 5개의 데이터를 갖음
    class State {
        private String tableName;          //데이터를 가져올 테이블 명
        private Finger[] finger;           //손가락 5개 데이터

        State(String tableName)
        {
            finger = new Finger[5];
            finger[FINGER.ON.getValue()] = new Finger("onfinger");      //엄지 데이터
            finger[FINGER.TW.getValue()] = new Finger("twfinger");
            finger[FINGER.TH.getValue()] = new Finger("thfinger");
            finger[FINGER.FO.getValue()] = new Finger("fofinger");
            finger[FINGER.FI.getValue()] = new Finger("fifinger");

            this.tableName = tableName;
        }
    }

    public Kmeans(){}

    public Kmeans(Context context){

        //state 초기화
        state = new State[2];
        state[STATE.BLOW.getValue()] = new State("blowData");       //굽힌 상태 객체 생성
        state[STATE.SPREED.getValue()] = new State("spreedData");   //펼친 상택 객체 생성

        this.context = context;         //디비에 접근하기 위한 context
    }

    public void init(){

        //데이터베이스 접근시 사용할 handler 초기화
        handler = new InputDataSQLiteOpenHandler[2];        //테이블 두개에 접근
        handler[0] = new InputDataSQLiteOpenHandler(context, "blowData");
        handler[1] = new InputDataSQLiteOpenHandler(context, "spreedData");

        //기준점 초기화
        center = new int[2][5];         //상태 두개 x 손가락 다섯개

        for(STATE s : STATE.values())
        {
            for(FINGER f : FINGER.values())
            {
                center[s.getValue()][f.getValue()] = new Integer(0);
            }
        }
    }

    //디비에서 데이터를 가져와 상태별 손가락 벡터에 저장
    public boolean inputData(){
        Vector<String> v = new Vector();

        for(STATE s : STATE.values())
        {
            for(FINGER f : FINGER.values()) {

                if((v = handler[s.getValue()].select(state[s.getValue()].finger[f.getValue()].columnName)).size() != 0)
                    state[s.getValue()].finger[f.getValue()].data = v;           //테이블이 비어있지 않을 경우 굽힘데이터 가져옴
                else//테이블에 데이터 없읍
                    return false;
            }
        }
        return true;
    }

    //상태, 손가락별 평균 연산
    private void calCenter(){

        int sum = 0;
        int length = 0;
        Vector<String> vector = new Vector();

        for(STATE s : STATE.values())
        {
            for(FINGER f : FINGER.values())
            {
                sum = 0;
                vector = state[s.getValue()].finger[f.getValue()].data;
                length = vector.size();

                for(String v : vector)
                    sum += Float.parseFloat(v);

                center[s.getValue()][f.getValue()] = sum/length;
                //Log.v(TAG, s.getValue() + "  " + f.getValue() + "   : " +  center[s.getValue()][f.getValue()]);
            }
        }
    }

    //기준점과 블루투스로 입력된 데이터를 비교하여 가장 가까운 상태 찾음
    //inputData : 블루투스로 입력된 데이터, return : 가장 가까운 상태 0(굽힘) 또는 1(펼침) 반환
    public String[] getResult(String[] inputData) {

        String[] result = new String[5];        //결과를 반환할 배열(굽힘, 펼침)
        float min = 0;      //최소값
        float value;        //현재 상태와 기준점을 뺀 값

        for(int i = 0; i < 5; i++)
            result[i] = new String("0");

        for(FINGER f : FINGER.values()) {
            //현재 상태와 기준점을 뺀 값
            min = Math.abs(center[STATE.BLOW.getValue()][f.getValue()] - Float.parseFloat(inputData[f.getValue()]));

             for(STATE s:STATE.values())
             {
                 value = Math.abs(center[s.getValue()][f.getValue()] - Float.parseFloat(inputData[f.getValue()]));
                 ////현재 상태와 기준점을 뺀 값이 가장 작은 값을 찾아서 넣음
                 if (min > value)
                 {
                     min = value;
                     result[f.getValue()] = String.valueOf(s.getValue());       //상태를 string으로 변환해서 결과저장
                 }

                 Log.v(TAG, f + " " + s + " 기준점과의 거리: " + center[s.getValue()][f.getValue()] + " -  " + inputData[f.getValue()]);
                 Log.v(TAG, f + " " + s + " 기준점과의 거리: " + value);
            }
            Log.v("AVG", "가장 가까운 상태 : " + f + " " + result[f.getValue()]);
        }

        return result;
    }

    public Boolean run(){

        init();

        //데이터베이스에서 받아온 데이터 저장
        if(inputData()){

            //중심 연산
            calCenter();
        }else{
           return false;
        }

        return true;
    }
}

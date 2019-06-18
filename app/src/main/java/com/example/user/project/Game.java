package com.example.user.project;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Vector;

import static com.example.user.project.FindSign.CODE.GAME;

enum QUIZ{
    BANANA(0), STAR(1), YODA(2);
    private int value;

    QUIZ(int value){ this.value = value; }
    int getValue(){ return value; }
}

class Stage{
    private QUIZ quizName;      //실행할 스테이지 명
    private int gifID;          //실행될 스테이지의 gif id
    private Vector<String> answer;
    private Vector<Integer> image;
    private boolean isOnGif;
    private Handler handler;

    Stage(QUIZ quizName, int gifID, Vector answer, Vector image){
        this.quizName = quizName;
        this.gifID = gifID;
        this.answer = answer;
        this.image = image;
        isOnGif = false;
        handler = new Handler();
    }

    public String getAnswer(){
        return answer.get(0);     //맞춰야 할 문자
    }

    public int getAnswerSize(){
        return answer.size();
    }

    public void popAnswer(){
        answer.remove(0);
        image.remove(0);
    }

    public int getGifID(){return gifID;}

    public void showImage(final ImageView gameAnswer){
        try {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    gameAnswer.setImageResource(image.get(0));
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

public class Game extends AppCompatActivity{
    private String TAG;
    private Vector<Stage> stage;

    //메인스레드에 접근할 핸들러
    private Handler handler;

    //UI
    private TextView timeText;
    private TextView extraText;
    private TextView gameSignText;
    private TextView dirText;
    private TextView gameFingerText;
    private int extraStage;
    private Button mainButton;
    private ImageView gameGif;
    private ImageView gameAnswer;
    //시간을 재생할 플래그
    private boolean timeFlag;

    //게임 제어 플래그
    private boolean gameFlag;
    int time = 0;       //플레이 시간

    //===========치트키=============
    //    private boolean isCheatKey;
    //    private Button cheatKeyBu;
    //=============================
    FindSign findSign;    //수화를 찾을 클래스
    String signResult;    //수화 결과

    @Override
    protected void onCreate(Bundle save){
        super.onCreate(save);

        setContentView(R.layout.game);

        TAG = "GAME";

        //================================================
//        isCheatKey = false;
//        cheatKeyBu = (Button)findViewById(R.id.cheatKey);
        //================================================

        //---------------UI 접근과 관련된 변수 초기화----------------
        timeText = (TextView)findViewById(R.id.timeText);
        extraText = (TextView)findViewById(R.id.extraText);
        mainButton = (Button)findViewById(R.id.mainButton);
        gameSignText = (TextView)findViewById(R.id.gameSignResult);
        gameAnswer = (ImageView)findViewById(R.id.gameAnswer);
        gameGif = (ImageView)findViewById(R.id.gameGif);
        dirText = (TextView)findViewById(R.id.gameDir);
        gameFingerText = (TextView)findViewById(R.id.gameFinger);
        handler = new Handler();
        //--------------------------------------------------------

        //-----------------수화와 관련된 변수 초기화----------------
        if(findSign == null)
            findSign = FindSign.getInstance(GAME);
        signResult = "";
        //--------------------------------------------------------

        //----------------게임과 관련된 변수 초기화------------------
        timeFlag = true;        //시간 스레드 플래그
        gameFlag = true;
        extraStage = 3;         //남은 게임 수
        extraText.setText("남은 문제 : " + extraStage + "");

        //스테이지
        Vector<String> vBanana = new Vector();
        Vector<String> vButterFly = new Vector();
        Vector<String> vYoda = new Vector();

        vBanana.add("ㅂ"); vBanana.add("ㅏ"); vBanana.add("ㄴ"); vBanana.add("ㅏ"); vBanana.add("ㄴ"); vBanana.add("ㅏ");
        vButterFly.add("ㄴ");  vButterFly.add("ㅏ"); vButterFly.add("ㅂ"); vButterFly.add("ㅣ");
        vYoda.add("ㅇ"); vYoda.add("ㅛ"); vYoda.add("ㄷ"); vYoda.add("ㅏ");

        //스테이지별 이미지
        Vector<Integer> vBananaImage = new Vector();
        Vector<Integer> vButterFlyImage = new Vector();
        Vector<Integer> vYodaImage = new Vector();

        vBananaImage.add(R.drawable.banana1); vBananaImage.add(R.drawable.banana2); vBananaImage.add(R.drawable.banana3); vBananaImage.add(R.drawable.banana4);
        vBananaImage.add(R.drawable.banana5); vBananaImage.add(R.drawable.banana6); vBananaImage.add(R.drawable.banana7);
        vButterFlyImage.add(R.drawable.fly1); vButterFlyImage.add(R.drawable.fly2); vButterFlyImage.add(R.drawable.fly3); vButterFlyImage.add(R.drawable.fly4);
        vButterFlyImage.add(R.drawable.fly5);
        vYodaImage.add(R.drawable.yoda1); vYodaImage.add(R.drawable.yoda2); vYodaImage.add(R.drawable.yoda3); vYodaImage.add(R.drawable.yoda4);
        vYodaImage.add(R.drawable.yoda5);

        stage = new Vector<Stage>();
        stage.add(new Stage(QUIZ.BANANA, R.raw.banana, vBanana, vBananaImage));
        stage.add(new Stage(QUIZ.BANANA, R.raw.butterfly, vButterFly, vButterFlyImage));
        stage.add(new Stage(QUIZ.BANANA, R.raw.yoda, vYoda, vYodaImage));

        stage.get(0).showImage(gameAnswer);
        //--------------------------------------------------------

        //메뉴로 가는 버튼
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        drawGif();          //현재 스테이지 gif출력
        timeThread();         //시간 스레드
        writeSignThread();        //수화 결과를 찾는 스레드
        playGame();         //게임 시작
    }

    private void timeThread(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(timeFlag){
                    try {
                        time++;
                        //signResult = findSign.getSignResult();      //수화 결과

                        final String myTime = time + "";
                        final String signText = signResult;
                        //-----------UI 수정-----------
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                timeText.setText("진행시간  " + myTime);
                                extraText.setText("남은 문제 : " + extraStage + "");
                            }
                        });
                        //-----------------------------

                        Thread.sleep(800);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });

        thread.start();
    }

    private void drawGif(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                //현재 스테이지
                Glide.with(getApplicationContext()).load(stage.get(0).getGifID()).into(gameGif);
            }
        });
    }

    //게임모드에선 잘못됬을 때 모음 다음에 모음이 와도 상관이 없잖아
    //그래서 인식이 안되는거네 이거 안고쳤음 큰일날뻔

    private void writeSignThread(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        String[] result = findSign.getResult();
                        signResult = findSign.getSignResult();      //수화 결과
                        final String fingerData = result[0] + "   " + result[1] + "   " + result[2] + "   " + result[3] + "   " + result[4];

                                //if(!signResult.equals("결과 없음"))
                        {       //수화 결과가 있을 경우에만 UI수정

                            final String signText = signResult;
                            final String dir = result[5];

                            //-----------UI 수정-----------
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    gameSignText.setText(signText);
                                    dirText.setText(dir);
                                    gameFingerText.setText(fingerData);
                                }
                            });
                            //-----------------------------
                        }

                        Thread.sleep(100);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    private void checkAnswer(){
        try {
            Stage presentStage = stage.get(0);        //현재 스테이지
            //if(presentStage.getAnswer().equals(signResult)){      //정답 맞춤
            if(presentStage.getAnswer().equals(signResult)){      //정답 맞춤
                Log.d(TAG, "정답 맞춤");
                if (presentStage.getAnswerSize() > 1) {         //맞춰야할 문자가 한개보다 많을 경우 다음 문제를 맞춤
                    presentStage.popAnswer();
                    presentStage.showImage(gameAnswer);

                } else if (presentStage.getAnswerSize() == 1) { //한 문제를 모두 맞춘 경우

                    presentStage.popAnswer();
                    presentStage.showImage(gameAnswer);         //답 보여줌
                    extraStage--;
                    Thread.sleep(600);

                    if (stage.size() > 1) {       //남아있는 스테이지가 있음

                        stage.remove(0);                  //맞춘 스테이지 제거
                        presentStage = stage.get(0);             //현재 스테이지 가져옴
                        presentStage.showImage(gameAnswer);      //현재 스테이지 이미지 출력

                        drawGif();              //현재 스테이지 gif출력

                    } else {    //게임 클리어
                        gameFlag = false;

                        Intent intent = new Intent(getApplicationContext(), GameClearActivity.class);
                        intent.putExtra("time", time);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void playGame(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(gameFlag) {
                    try {
                        //readSign();          //번역 결과를 받아오는 스레드
                        checkAnswer();       //답이 맞는지 확인

                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }
    @Override
    protected void onResume(){
        super.onResume();

        findSign.run();

        gameFlag = true;
        timeFlag = true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameFlag = false;
        timeFlag = false;
    }
}

package com.example.user.project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;

import java.util.Vector;

//데이터를 입력받을 함수
public class InputData extends AppCompatActivity {

    //작업스레드가 메인스레드로 변수를 전달 할 핸들러
    protected Handler hander = new Handler();

    //DB에 접근 하는 핸들러
    protected InputDataSQLiteOpenHandler dbHandler;
    protected String tableName;

    //반복문의 플래그
    protected boolean flag = true;

    //모은 데이터
    Vector<Vector<Integer>> fingerData;

    //-----------palette-----------
    protected ProgressBar Bar;
    protected Button btn;
    protected TextView text;
    protected TextView sayText;
    protected int layout;
    //-----------------------------

    //다음에 수행될 클래스
    Class<?> nextClass;

    BluetoothService btService = null;

    //자식 클래스로부터 전달받을 생성자
    public InputData(int layout, Class<?> nextClass, String tableName)
    {
        this.layout=layout;
        this.nextClass = nextClass;     //데이터 삽입 후 실행될 액티비티
        this.tableName = tableName;

        if(btService == null)
            btService = BluetoothService.getInstance();

        android.util.Log.v("MyTag",tableName);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(layout);

        //데이터베이스를 접근하기 위한 핸들러
        dbHandler = new InputDataSQLiteOpenHandler(getApplicationContext(), tableName);

        //xml 연결부
        text = (TextView)findViewById(R.id.guidText);
        Bar = (ProgressBar)findViewById(R.id.progressBar);
        btn = (Button)findViewById(R.id.button);
        sayText = (TextView)findViewById(R.id.sayText);

        fingerData = new Vector<>();

        //findSign.run();

        InputDataThread();
    }

    private void InputDataThread(){
        //Bar.setProgress(50);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int valueTmp = 0;
                Log.d("thread", "InputDataThread() 실행");

                while(flag)
                {
                    Vector<Integer> a = new Vector<>();

                    String[] temp = btService.getResult();

                    int[] arr = new int[5];
                    arr[0] = Integer.parseInt(temp[0]); arr[1] = Integer.parseInt(temp[1]); arr[2] = Integer.parseInt(temp[2]);
                    arr[3] = Integer.parseInt(temp[3]); arr[4] = Integer.parseInt(temp[4]);

                    a.add(arr[0]);
                    a.add(arr[1]);
                    a.add(arr[2]);
                    a.add(arr[3]);
                    a.add(arr[4]);

                    fingerData.add(a);

                    valueTmp +=1;

                    try{
                        final int value = valueTmp;
//                                //---------메인스레드로 전달---------
                        hander.post(new Runnable() {
                            @Override
                            public void run() {

                                Bar.setProgress(value);
                                text.setText("데이터베이스에 " + String.valueOf(value) + " / " + String.valueOf(100) + "개 데이터가 삽입되었습니다.");

                                if (value > 100) {
                                    try{
                                        final String[] str = {"onfinger", "twfinger", "thfinger" , "fofinger" , "fifinger"};          //컬럼 이름

                                        flag = false;
                                        dbHandler.insert(str, fingerData);        //데이터 추가부분
                                        text.setText("완료되었습니다.");

                                        Thread.sleep(100);
                                        Intent intent = new Intent(getApplicationContext(), nextClass);
                                        startActivity(intent);

                                        finish();

                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                }else if(value == 1){
                                    sayText.setText("주먹을 꽉 쥐세요");
                                }else if(value == 20){
                                    sayText.setText("손을 피고 싶을땐");
                                }else if(value == 40){
                                    sayText.setText("싫어하는 사람을 떠올려보세요");
                                }else if(value == 60){
                                    sayText.setText(".");
                                }else if(value == 80){
                                    sayText.setText("이제 절반왔는데 할말이 없네.");
                                }else if(value == 90){
                                    sayText.setText("그만할래");
                                }

                                DrawRect drawRect = (DrawRect)findViewById(R.id.can);
                                drawRect.invalidate();                  //draw호출


                            }
                        });
////                                //------------전달 완료--------------

                        Thread.sleep(100);

                    }catch (Exception e){e.printStackTrace();}
                }

                Log.d("thread", "InputDataThread() 종료");
            }
        });

        thread.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        flag = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        flag = true;
        btn.performClick();
    }
}


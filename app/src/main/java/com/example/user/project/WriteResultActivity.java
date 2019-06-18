package com.example.user.project;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.example.user.project.FindSign.CODE.TRANS;

public class WriteResultActivity extends AppCompatActivity {

    private TextView accSignResultText;         //누적된 번역 결과를 기록하는 텍스트뷰
    private TextView dirText;
    private String accSignResult;               //findSign으로부터 얻은 누적된 해석 결과
    private String dir;
    private FindSign findSign;      //번역해주는 클래스
    private boolean flag;           //화면에 기록할 스레드
    private Handler handler;        //메인스레드로 작업을 넘길 핸들러
    private String TAG = "CheckResultActivity";
    private Button backSpace;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.print_result);

        accSignResult = "";
        dir = "";

        accSignResultText = (TextView)findViewById(R.id.accSignResult);
        dirText = (TextView)findViewById(R.id.dirText);

        flag = true;
        handler = new Handler();


        backSpace = (Button)findViewById(R.id.backSpace);

        //글자 지움
        backSpace.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                findSign.callDeleteWord();
            }
        });

        Log.v(TAG, "CheckResultActivity onCreate 시작");
    }

    public void writeTextView(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("thread", "writeTextView() thread 실행");
                while(flag) {
                    try{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                        accSignResult = findSign.getAccSignResult();
                                        accSignResultText.setText(accSignResult);

                                        dir = findSign.getResult()[5];
                                        dirText.setText(dir);
                                }
                            });

                        Thread.sleep(500);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                Log.d("thread", "writeTextView() thread 종료");
            }
        });
        thread.start();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(findSign == null)
            findSign = FindSign.getInstance(TRANS);
        findSign.run();     //번역 시작
        flag = true;
        writeTextView();
    }

    @Override
    public void onPause(){
        super.onPause();

        findSign.stop();
        findSign = null;
        flag = false;
        System.gc();
    }

}

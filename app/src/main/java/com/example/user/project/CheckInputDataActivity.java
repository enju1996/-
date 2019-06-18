package com.example.user.project;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import static java.lang.Math.abs;


public class CheckInputDataActivity extends AppCompatActivity {
    private TextView[] result;                 //화면에 출력될 결과(구부림, 펼침 및 방향 결과)
    private TextView[] data;                   //화면에 출력될 데이터(센서 인풋)
    private TextView signResult;               //화면에 출력될 결과(번역 결과)
    private  FindSign findSign;                 //번역을 수행하는 클래스
    private  String TAG = "compare";
    private Handler handler;                   //메인스레드로 작업을 넘길 핸들러
    private boolean flag = true;
    private TextView[] text;

    //---프로글래스 생성시 필요한 변수---
    static ProgressDialog progressDialog;
    private MyDialogFragment frag;
    private StableAsyncTask stableAsyncTask;
    //--------------------------------

    private String[] myResult;          //구부림, 펼침, 방향
    private int[] myData;                //데이터
    private String mySignResult;        //번역결과

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(CheckInputDataActivity.this);

        myData = new int[6];
        myResult= new String[6];
        text = new TextView[5];

        for(int i = 0; i < myData.length; i++)
            myData[i] = 0;

        result = new TextView[6];           //굽힘, 방향 번역 결과
        data = new TextView[5];             //측정된 데이터
        handler = new Handler();


        setContentView(R.layout.print_data);
        //setContentView(drawRect);

        result[0] = (TextView) findViewById(R.id.RESULT0);
        result[1] = (TextView) findViewById(R.id.RESULT1);
        result[2] = (TextView) findViewById(R.id.RESULT2);
        result[3] = (TextView) findViewById(R.id.RESULT3);
        result[4] = (TextView) findViewById(R.id.RESULT4);
        result[5] = (TextView) findViewById(R.id.DIRESULT);

        data[0] = (TextView) findViewById(R.id.DATA0);
        data[1] = (TextView) findViewById(R.id.DATA1);
        data[2] = (TextView) findViewById(R.id.DATA2);
        data[3] = (TextView) findViewById(R.id.DATA3);
        data[4] = (TextView) findViewById(R.id.DATA4);
//        data[5] = (TextView) findViewById(R.id.DIRDATA);
//        signResult = (TextView) findViewById(R.id.SIGNRESULT);

    }

    @Override
    public void onResume(){
        super.onResume();

        Log.d(TAG, "-----onResume-----");

        if(findSign == null)
            findSign = FindSign.getInstance(FindSign.CODE.valueOf("TRANS"));

        findSign.run();     //찾기 시작
        flag = true;        //화면에 쓰는 스레드 시작

        //startSubThread();

        writeTextView();    //검색 결과를 화면에 반영해줌
    }

    @Override
    public void onPause(){
        super.onPause();

        Log.d(TAG, "------onPause-----");
        findSign.stop();    //findSign에서 생성된 스레드 종료
        flag = false;       //화면에 쓰기 중단

        System.gc();        //객체 소멸시킴
    }

    @Override
    public void onBackPressed()
    {
        stableAsyncTask.cancel(true);
        finish();
    }

    public void waitStable()        //안정화 작업 중에는 로딩창을 띄움
    {
        if (stableAsyncTask == null) {
            stableAsyncTask = new StableAsyncTask();
            stableAsyncTask.execute(1000);
        }
    }

    public void writeTextView() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                waitStable();       //안정화가 완료될 때 까지 프로글래스바 생성
                Log.d("thread", "writeTextView() thread 실행");
                while (flag) {
                    try {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //-----------------findSign으로 얻어온 결과-------------
                                String[] myResult = findSign.getResult();           //구부림, 펼침, 방향
                                Log.d("확인중", "CheckInputData   myResult[5] 그리고 씀" + myResult[5]);

                                int[] myData = findSign.getData();               //데이터
                                String mySignResult = findSign.getSignResult();     //번역결과
                                //-----------------------------------------------------

                                //-------------------화면으로 결과 출력------------------
                                for (int i = 0; i < result.length; i++) {
                                    result[i].setText(myResult[i]);
                                }

                                for (int i = 0; i < data.length; i++) {
                                    data[i].setText(myData[i]+"");
                                }
//
//                                signResult.setText(mySignResult);
//                                //------------------------------------------------------

                                DrawRect drawRect = (DrawRect)findViewById(R.id.can);
                                drawRect.invalidate();                  //draw호출
                            }
                        });

                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.d("thread", "writeTextView() thread 종료");
            }
        });

        thread.start();
    }

    class StableAsyncTask extends AsyncTask<Integer, Integer, String> {      //로딩창을 생성할 AsyncTask
        private  boolean isStable;       //센서의 안정화 여부

        @Override
        protected void onPreExecute() {      //프로글래스 생성
            super.onPreExecute();

            frag = MyDialogFragment.newInstance();
            frag.show(getFragmentManager(), "TAG");

        }

        @Override
        protected String doInBackground(Integer... params) {     //로딩중 수행할 내용

            while (!isStable) {
                try {
                    isStable = findSign.getStable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return "작업 완료";
        }

        @Override
        protected void onPostExecute(String result) {        //doInBackground가 모두 수행된 후 수행할 내용
            super.onPostExecute(result);

            frag.dismiss();
        }

    }//StableAsyncTask 끝

    public static class MyDialogFragment extends DialogFragment {
        public static MyDialogFragment newInstance() {
            return new MyDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("센서 안정화가 진행중입니다.");
            progressDialog.setMessage("안정화 진행중...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            return progressDialog;
        }
    }//MyDialogFragment 끝

}
package com.example.user.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

////데이터 베이스 보려면 풀것
//import com.amitshekhar.DebugDB;
import android.os.Handler;
import android.widget.Toast;

import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private Button inputDataBu;
    private Button compareFingerBu;
    private Button checkResultBu;
    private Button dropDataBu;
    private Button gameBu;
    private String TAG = "MAIN";

    private BluetoothService btService = null;

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static final int MESSAGE_READ = 3;
    public static final int MESSAGE_WRITE = 4;

    private static AppCompatActivity sApplication;

    private final static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public static Handler getmHandler(){
        return mHandler;
    }

    public static AppCompatActivity getsApplication(){
        return sApplication;
    }

    public static Context getContext(){
        return getsApplication().getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //main의 context를 정적으로 사용
        sApplication = this;

        //상태 바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // BluetoothService 클래스 생성
        if(btService == null)
        {
            btService = BluetoothService.getInstance();
        }

//        //데이터 베이스 보려면 풀것
//        Log.d(TAG, "☆☆☆☆☆☆☆☆☆☆☆☆디비가 여깄다☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
//        DebugDB.getAddressLog();

        inputDataBu = (Button)findViewById(R.id.inputDataBu);
        compareFingerBu = (Button)findViewById(R.id.dataBu);
        checkResultBu = (Button)findViewById(R.id.checkResultBu);
        dropDataBu = (Button)findViewById(R.id.dropDataBu);
        gameBu = (Button)findViewById(R.id.gameBu);


        //데이터 삽입
        inputDataBu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, btService.isRecive + "");

                if(btService.getState() != btService.STATE_CONNECTED) {     //블루투스가 연결되어있지 않을 경우
                    createBluetoothDialog();
                }else if(!BluetoothService.isRecive){           //아두이노로부터 데이터가 넘어오고 있지 않을 경우
                    createArduDialog();
                }else
                    {
                    Intent intent = new Intent(getApplicationContext(), InputDataActivity.class);
                    startActivity(intent);
                }
            }
        });

        //테이블 삭제
        dropDataBu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                InputDataSQLiteOpenHandler handler = new InputDataSQLiteOpenHandler(getApplicationContext(), "");
                handler.dropTable();

                Toast.makeText(getApplicationContext(), "데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //결과 확인
        checkResultBu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(btService.getState() != btService.STATE_CONNECTED) {     //블루투스 연결 확인
                    createBluetoothDialog();
                    //finish();
                }else if(isEmpty()){        //데이터 삽입 여부 확인
                    createDataDialog();
                }else if(!btService.isRecive){           //아두이노로부터 데이터가 넘어오고 있지 않을 경우
                    createArduDialog();
                } else {
                    Intent intent = new Intent(getApplicationContext(), WriteResultActivity.class);
                    startActivity(intent);
                }
            }
        });

        //손가락 값 비교
        compareFingerBu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Log.d("MAIN", "findSign.run()");
                if(btService.getState() != btService.STATE_CONNECTED) {
                    createBluetoothDialog();
                }else if(isEmpty()){
                    createDataDialog();
                }else if(!btService.isRecive){           //아두이노로부터 데이터가 넘어오고 있지 않을 경우
                    createArduDialog();
                } else {
                    Intent intent = new Intent(getApplicationContext(), CheckInputDataActivity.class);
                    startActivity(intent);
                }
            }
        });

        gameBu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btService.getState() != btService.STATE_CONNECTED) {
                    createBluetoothDialog();
                }else if(isEmpty()){
                    createDataDialog();
                }
                else if(!BluetoothService.isRecive){           //아두이노로부터 데이터가 넘어오고 있지 않을 경우
                    createArduDialog();
                }
                else
                    {
                    Intent intent = new Intent(getApplicationContext(), Game.class);
                    startActivity(intent);
                }
            }
        });
    }

    public boolean isEmpty(){       //테이블이 비어있는지 확인
        InputDataSQLiteOpenHandler[] handler = new InputDataSQLiteOpenHandler[2];
        handler[0] = new InputDataSQLiteOpenHandler(getApplicationContext(), "blowData");
        handler[1] = new InputDataSQLiteOpenHandler(getApplicationContext(), "spreedData");

        Vector<String> v1 = handler[0].select("onfinger");
        Vector<String> v2 = handler[1].select("onfinger");

        if(v1.size() == 0 || v2.size() == 0)
            return true;

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {         //액션바 메뉴 생성
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {           //버튼 클릭 시 이벤트 발생
        int id = item.getItemId();

        if(id == R.id.newPost){

            if (btService.getDeviceState()) {
                // 블루투스가 지원 가능한 기기일 때
                btService.enableBluetooth();
            } else {
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT: // When the request to enable Bluetooth returns

                if (resultCode == Activity.RESULT_OK)
                { // 확인 눌렀을 때 //Next Step
                    Log.d(TAG, "Next Step");
                    btService.scanDevice();
                }
                else
                { // 취소 눌렀을 때
                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;

            case REQUEST_CONNECT_DEVICE: // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
                    Log.d(TAG, "conn device");
                    btService.getDeviceInfo(data);

                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

                    //progressBar 만들기
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progressDialog.setMessage("블루투스 연결중...");
                                    progressDialog.show();
                                }
                            });
                            try {
                                while (btService.getState() != btService.STATE_CONNECTED)
                                {
                                    Thread.sleep(300);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            progressDialog.dismiss();
                        }
                    }
                    ).start();

                }
                break;

        }
    }

    public void createBluetoothDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 연결되어있지 않음").setMessage("블루투를 연결하세요").setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void createArduDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("아두이노 에러");
        builder.setMessage("아두이노가 실행중이지 않음");
        builder.setPositiveButton("예",null);

        builder.create().show();
    }

    public void createDataDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("삽입된 값 없음");
        builder.setMessage("데이터를 삽입하세요");
        builder.setPositiveButton("예",null);

        builder.create().show();

    }

}

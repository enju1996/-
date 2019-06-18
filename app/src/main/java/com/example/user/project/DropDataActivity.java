package com.example.user.project;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class DropDataActivity extends AppCompatActivity {       //삽입된 손가락 데이터 테이블을 비워줌
    private InputDataSQLiteOpenHandler dbHandler;

    @Override
    public void onCreate(Bundle save)
    {
        super.onCreate(save);

        dbHandler = new InputDataSQLiteOpenHandler(getApplicationContext(), "");
        dbHandler.dropTable();

        Toast.makeText(this, "데이터가 삭제되었습니다.", Toast.LENGTH_SHORT);
    }
}

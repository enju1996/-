package com.example.user.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

//import com.amitshekhar.DebugDB;

public class InputDataActivity extends AppCompatActivity {
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.input_data);

        btnStart = (Button)findViewById(R.id.initBtn);
        btnStart.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //DebugDB.getAddressLog();

                        Intent intent = new Intent(getApplicationContext(),InitBlowData.class);
                        startActivity(intent);

                        finish();
                    }
                }
        );
    }
}

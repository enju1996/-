package com.example.user.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class InitData extends AppCompatActivity{
    private Button btn;
    private  int layout;
    private Class<?> nextClass;
    private int nextButton;
    private int gifID;
    private int gifViewID;
    private ImageView gifView;
    InitData(){}

    InitData(int layout, Class<?> nextClass,  int nextButton, int gifViewID, int gifID){
        this.layout = layout;
        this.nextClass = nextClass;
        this.nextButton = nextButton;
        this.gifID = gifID;
        this.gifViewID = gifViewID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(layout);

        btn = findViewById(nextButton);
        gifView = findViewById(gifViewID);

        Glide.with(getApplicationContext()).load(gifID).into(gifView);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), nextClass);
                startActivity(intent);
                finish();
            }
        });

    }
}

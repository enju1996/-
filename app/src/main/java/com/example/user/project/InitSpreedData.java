package com.example.user.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class InitSpreedData extends InitData {
    public InitSpreedData(){
        super(R.layout.init_spreed, InputDataSpreedActivity.class, R.id.spreedBu, R.id.spreadhandgif, R.raw.handwaving);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

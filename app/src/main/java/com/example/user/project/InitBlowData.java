package com.example.user.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class InitBlowData extends InitData {
    public InitBlowData(){
        super(R.layout.init_blow, InputDataBlowActivity.class, R.id.blowBu, R.id.blowhandgif, R.raw.handblow);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

package com.example.user.project;
import android.os.Bundle;

public class InputDataBlowActivity extends InputData {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public InputDataBlowActivity(){
        super(R.layout.inputdata_blow, InitSpreedData.class, "blowData");
        android.util.Log.v("MyTag","자식생성자 실행");
    }
}

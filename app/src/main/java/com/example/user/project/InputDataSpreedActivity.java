package com.example.user.project;
import android.os.Bundle;

public class InputDataSpreedActivity extends InputData {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public InputDataSpreedActivity(){
        super( R.layout.inputdata_spreed, MainActivity.class, "spreedData");
        android.util.Log.v("MyTag","자식생성자 실행");
    }
}

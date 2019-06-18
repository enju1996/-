package com.example.user.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

public class GameClearActivity extends AppCompatActivity {
    private TextView scoreText;
    private TextView topScoreText;
    private  int time;
    private int topScore;
    private ImageView newscore;
    private InputDataSQLiteOpenHandler dbhandler;
    private String TAG;

    @Override
    protected void onCreate(Bundle save) {
        super.onCreate(save);

        setContentView(R.layout.game_clear);

        scoreText = (TextView)findViewById(R.id.game_score);
        topScoreText = (TextView)findViewById(R.id.topScore);
        newscore = (ImageView)findViewById(R.id.newscore_img);
        TAG = "GAME";

        newscore.setVisibility(View.INVISIBLE);

        dbhandler = new InputDataSQLiteOpenHandler(getApplicationContext(), "sort");

        Intent intent = getIntent();
        time = intent.getExtras().getInt("time");


        Vector<String> vtopScore = dbhandler.select("score");

        if(vtopScore.size() == 1){                      //최고기록이 없거나 기록을 갱신했을 경우
            Log.d(TAG, "최고기록 없음");
            Log.d(TAG, "디비에 삽입되어 있는 기록  :  " + vtopScore.get(0));

            newscore.setVisibility(View.VISIBLE);       //이미지 보이게 함
            dbhandler.insert("score", time);       //최고기록으로 추가
            topScore = time;

        }else if( (time < Integer.parseInt(vtopScore.get(1)))){                      //최고기록이 없거나 기록을 갱신했을 경우
            Log.d(TAG, "갱신됨.   time : " + time + "   topScore  :  " + topScore);
            newscore.setVisibility(View.VISIBLE);       //이미지 보이게 함
            dbhandler.deleteTopScore();                     //이전 기록 삭제
            dbhandler.insert("score", time);       //최고기록으로 추가
            topScore = time;
        }else {
            topScore = Integer.parseInt(vtopScore.get(1));
        }

        setScore();
    }

    private void setScore(){
        Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                scoreText.setText(time + "");
                topScoreText.setText(topScore + "");
            }
        });
    }
}

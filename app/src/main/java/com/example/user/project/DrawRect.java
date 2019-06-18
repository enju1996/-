package com.example.user.project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.example.user.project.FindSign.CODE.TRANS;

class DrawRect extends View {
    Context context;
    MyRect[] myrect;    //사각형
    int[] myData;       //굽힘 수치
    int width;          //사각형의 폭
    int stdX;           //사각형이 시작하는 x
    int stdY;           //사각형이 시작하는 y
    TextView textView;
    BluetoothService btService = null;
    FindSign findSign = null;

    public DrawRect(Context context, AttributeSet attr) {
        super(context, attr);

        this.context = context;

        if(btService == null)
            btService = BluetoothService.getInstance();

        if(findSign == null)
            findSign = FindSign.getInstance(TRANS);

        myrect = new MyRect[5];
        myData = new int[5];
        textView = new TextView(context);

        //-----------사용자의 화면의 크기를 가져옴-----------
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        width = size.x / 5;
        stdX = 0;
        stdY = (int)(size.y /2);          //다비이스 높이

        //------------------------------------------------

        //상자 그리기
        for (int i = 0; i < myrect.length; i++) {
            myrect[i] = new MyRect(stdX + (i * width), context);
            Log.d("RECT", "myrect[" + i + "]  " + myrect[i].getX());
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String[] result = findSign.getResult();

            Paint pnt = new Paint();      //패인트
            pnt.setStrokeWidth(10f);
            pnt.setColor(Color.parseColor("#D7F4F7"));
            pnt.setStyle(Paint.Style.STROKE);


            canvas.drawRGB(255, 255, 255);

        if (result != null) {
            RectF rect = new RectF();

            for (int i = 0; i < 5; i++) {

                if (result[i].equals("펼침")) {     //폈을 때
                    rect.set(myrect[i].getX(), stdY - 500, myrect[i].getX() + width, stdY + 500);
                } else if (result[i].equals("굽힘")) {     //살짝 굽었을 때
                    rect.set(myrect[i].getX(), stdY - 100, myrect[i].getX() + width, stdY + 100);
                }

                canvas.drawRect(rect, pnt);
            }
        }else{
            //캔버스로 화면어떻게지움 ㅡㅡ
        }
    }
}

class MyRect{
    private int x;       //사각형 시작하는 x
    private Context context;

    MyRect(int x, Context context){      //x는 사각형이 시작하는 위치

        this.x = x;
        this.context = context;

    }

    public int getX(){return x;}
}

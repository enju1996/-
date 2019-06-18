package com.example.user.project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class InputDataSQLiteOpenHelper extends SQLiteOpenHelper {
    private String tableName;
    public InputDataSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int verson, String tableName){
        super(context, name, factory, verson);

        this.tableName = tableName;

        Log.v("MyTag", "helper 생성자 tableName : " + tableName);
    }


    @Override
    //데이터베에스 최초 생성시에만 호출
    public void onCreate(SQLiteDatabase db){
        String sql= "create table " + tableName + "(_id integer primary key autoincrement, onfinger float, twfinger float, thfinger float, fofinger float, fifinger float)";
        db.execSQL(sql);

        Log.v("MyTag", "helper onCreate" + sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        String sql = "";
        db.execSQL(sql);

        onCreate(db);
    }
}


package com.example.user.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Vector;

public class InputDataSQLiteOpenHandler {
    private InputDataSQLiteOpenHelper helper;
    private  SQLiteDatabase db;
    private String tableName;

    public InputDataSQLiteOpenHandler(Context context, String tableName){
        String sql;

        this.tableName = tableName;
        Log.v("MyTag", "handler 생성자 tableName : " + this.tableName);

        helper = new InputDataSQLiteOpenHelper(context, "project.db", null, 1, tableName);

        db = helper.getWritableDatabase();

        if(tableName == "blowData" || tableName == "spreedData") {
            sql = "create table if not exists " + tableName + "(_id integer primary key autoincrement, onfinger integer, twfinger integer, thfinger integer, fofinger integer, fifinger integer);";
            db.execSQL(sql);
        }else if(tableName == "sign") {

            //_id : 인덱스, ofinger~fifinger : 엄지~새끼, dir : 방향, word : 0(자음 ㄱㄴㄷㄹ), 1(모음 ㅏㅑㅓㅕ)
            //dir : 방향 (0: 손등 왼쪽, 1: 손등 아래쪽, 2: 손등 위쪽 X, 3: 손바닥 위쪽, 4: 측면)
            sql = "create table if not exists sign (_id integer primary key autoincrement, mean CHAR(1), ofinger INT, twfinger INT, thfinger INT, fofinger INT, fifinger INT, dir INT, word INT);";
            db.execSQL(sql);

            sql = "INSERT OR REPLACE INTO sign VALUES(1, 'ㅌ', 0, 1, 1, 1, 0, 0, 0), (2, 'ㄴ', 1, 1, 0, 0, 0, 0, 0), (3, 'ㄷ', 0, 1, 1, 0, 0, 0, 0), (4, 'ㄹ', 0, 1, 1, 1, 0, 0, 0), (5, 'ㅡ', 0, 1, 0, 0, 0, 0, 1)" +
                    ", (6, 'ㄱ', 1, 1, 0, 0, 0, 1, 0), (7, 'ㅅ', 0, 1, 1, 0, 0, 1, 0), (8, 'ㅈ', 1, 1, 1, 0, 0, 1, 0), (9, 'ㅊ', 1, 1, 1, 1, 0, 1, 0), (10, 'ㅋ', 1, 0, 1, 0, 0, 1, 0)" +
                    ", (11, 'ㅜ', 0, 1, 0, 0, 0, 1, 1), (12, 'ㅠ', 0, 1, 1, 0, 0, 1, 1)" +
                    ", (13, 'ㅗ', 0, 1, 0, 0, 0, 2, 1), (14, 'ㅛ', 0, 1, 1, 0, 0, 2, 1)" +
                    ", (15, 'ㅁ', 0, 0, 0, 0, 0, 3, 0), (16, 'ㅂ', 0, 1, 1, 1, 1, 3, 0), (17, 'ㅍ', 0, 0, 0, 0, 0, 3, 0), (18, 'ㅏ', 0, 1, 0, 0, 0, 3, 1), (19, 'ㅑ', 0, 1, 1, 0, 0, 3, 1), (20, 'ㅇ', 0, 0, 1, 1, 1, 3, 0)" +
                    ", (21, 'ㅣ', 0, 0, 0, 0, 1, 3, 1), (22" +
                    ", 'ㅐ', 0, 1, 0, 0, 1, 3, 1), (23, 'ㅒ', 0, 1, 1, 0, 1, 3, 1)" +
                    ", (24, 'ㅎ', 1, 0, 0, 0, 0, 4, 0), (25, 'ㅓ', 0, 1, 0, 0, 0, 4, 1), (26, 'ㅕ', 0, 1, 1, 0, 0, 4, 1), (27, 'ㅔ', 0, 1, 0, 0, 1, 4, 1), (28, 'ㅖ', 0, 1, 1, 0, 0, 4, 1)";


            db.execSQL(sql);
        }else if(tableName == "sort") {
            sql = "create table if not exists sort (_index Integer primary key autoincrement, score INT)";
            db.execSQL(sql);

            sql = "INSERT OR REPLACE INTO sort VALUES(1, 99999999)";
            db.execSQL(sql);
        }
    }

    public InputDataSQLiteOpenHandler open(Context context){
        return new InputDataSQLiteOpenHandler(context, tableName);
    }

    public void close(){
        helper.close();
    }

    public void insert(String[] attri, Vector<Vector<Integer>> value){
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();

            for (int j = 0; j < value.get(0).size(); j++) {
                for(int i = 0; i < attri.length; i++) {
                     values.put(attri[i], value.get(j).get(i));
                }
                db.insert(tableName, null, values);
            }
    }

    public void insert(String attri, int value){
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(attri, value);

        db.insert(tableName, null, values);
    }

    public Vector<String> select(String column){

        Vector<String> value = new Vector<String>();

        db = helper.getReadableDatabase();

        String sql = "select " + column + " from " + tableName;
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            value.add(cursor.getString(cursor.getColumnIndex(column)));
        }

        return value;

    }

    public void dropTable(){
        String sql = "delete from blowData";
        db.execSQL(sql);

         sql = "delete from spreedData";
        db.execSQL(sql);
    }

    public void deleteData(){
        String sql = "delete from " + tableName;
        db.execSQL(sql);
    }

    public void deleteTopScore(){
        String sql = "delete from " + tableName + " where _index NOT LIKE '1'";
        db.execSQL(sql);
    }
}

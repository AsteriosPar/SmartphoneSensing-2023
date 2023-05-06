package com.tudelft.indoorlocalizationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseClass extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "KNN_Database";
    private static final String AP = "access_points";

    public DatabaseClass(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE C1 (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + AP + " TEXT, M0 INTEGER, M1 INTEGER, M2 INTEGER, M3 INTEGER, M4 INTEGER, M5 INTEGER, M6 INTEGER, M7 INTEGER, M8 INTEGER, M9 INTEGER)");
        db.execSQL("CREATE TABLE C2 (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + AP + " TEXT, M0 INTEGER, M1 INTEGER, M2 INTEGER, M3 INTEGER, M4 INTEGER, M5 INTEGER, M6 INTEGER, M7 INTEGER, M8 INTEGER, M9 INTEGER)");
        db.execSQL("CREATE TABLE C3 (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + AP + " TEXT, M0 INTEGER, M1 INTEGER, M2 INTEGER, M3 INTEGER, M4 INTEGER, M5 INTEGER, M6 INTEGER, M7 INTEGER, M8 INTEGER, M9 INTEGER)");
        db.execSQL("CREATE TABLE C4 (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + AP + " TEXT, M0 INTEGER, M1 INTEGER, M2 INTEGER, M3 INTEGER, M4 INTEGER, M5 INTEGER, M6 INTEGER, M7 INTEGER, M8 INTEGER, M9 INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS C1");
        db.execSQL("DROP TABLE IF EXISTS C2");
        db.execSQL("DROP TABLE IF EXISTS C3");
        db.execSQL("DROP TABLE IF EXISTS C4");
        onCreate(db);
    }
    public boolean checkAPExists(String access_point, String cell) {
        boolean nameExists = false;

        SQLiteDatabase db = this.getReadableDatabase();
        String st = "select * from " + cell + " where " + AP + " = '" + access_point + "'";
        Cursor data = db.rawQuery(st, null);
        if(data.moveToNext())
        {
            nameExists = true;
        }
        return nameExists;
    }

    public boolean addData(String ar, int signal_strength, String cell, String cur_column) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!checkAPExists(ar, cell)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AP, ar);
            contentValues.put(cur_column, signal_strength);
            long result = db.insert(cell, null, contentValues);
            return result != -1;
        }
        else {
            String query = "UPDATE " + cell + " SET " + cur_column + " = '" + signal_strength + "' WHERE " + AP + " = '" + ar + "'";
            db.execSQL(query);
        }
        return true;
    }
    public void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM C1");
        db.execSQL("DELETE FROM C2");
        db.execSQL("DELETE FROM C3");
        db.execSQL("DELETE FROM C4");
    }

    public void deleteData(String cell){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ cell);
    }
}

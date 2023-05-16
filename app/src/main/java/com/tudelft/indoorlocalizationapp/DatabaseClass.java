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
    private static final String CELL_NAME = "CELL_NAME";
    private static final String CELLS = "CELLS";

    public DatabaseClass(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE CELLS (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + CELL_NAME + " TEXT, MEASUREMENTS INTEGER DEFAULT 0)");
    }

    public void addCell(String cell_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE " + cell_name + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + AP + " TEXT, M0 INTEGER, M1 INTEGER, M2 INTEGER, M3 INTEGER, M4 INTEGER, M5 INTEGER, M6 INTEGER, M7 INTEGER, M8 INTEGER, M9 INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS CELLS");
        db.execSQL("DROP TABLE IF EXISTS C1");
        db.execSQL("DROP TABLE IF EXISTS C2");
        db.execSQL("DROP TABLE IF EXISTS C3");
        db.execSQL("DROP TABLE IF EXISTS C4");
        onCreate(db);
    }
    public boolean checkAPExists(String access_point, String cell) {
        SQLiteDatabase db = this.getReadableDatabase();
        String st = "select * from " + cell + " where " + AP + " = '" + access_point + "'";
        Cursor data = db.rawQuery(st, null);
        return data.moveToNext();
    }

    public boolean addData(String ar, int signal_strength, String cell, String cur_column) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT MEASUREMENTS FROM " + CELLS + " WHERE " + CELL_NAME + " = '" + cell + "'", null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.getInt(0);
        }


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

    public boolean isNull(String access_point, String cell, String measurement) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + measurement + " FROM " + cell + " WHERE " + AP + " = '" + access_point + "'", null);

        if (cursor != null && cursor.moveToFirst()) {
            return cursor.isNull(0);
        }
        return false;
    }
}

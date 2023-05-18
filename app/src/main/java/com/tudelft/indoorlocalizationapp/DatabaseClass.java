package com.tudelft.indoorlocalizationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseClass extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Bayesian_Database";
    private static final String AP = "access_points";

    public DatabaseClass(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //        Add table that stores the measurements of each cell
        db.execSQL("CREATE TABLE MEASUREMENTS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, COLUMNS INTEGER)");

        for (int i = 1; i < 21; i++) {
            String query = "CREATE TABLE C" + i + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," + AP + " TEXT";
            for (int j = 1; j < 41; j++) {
                query += ", M" + j + " INTEGER";
            }
            query += ")";
            db.execSQL(query);

            ContentValues contentValues = new ContentValues();
            contentValues.put("NAME", "C"+i);
            contentValues.put("COLUMNS", 0);
            db.insert("MEASUREMENTS", null, contentValues);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        for (int j = 1; j < 21; j++) {
            db.execSQL("DROP TABLE IF EXISTS C" + j);
        }
        onCreate(db);
    }
    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int j = 17; j < 21; j++) {
            db.execSQL("DELETE FROM C" + j);
            String query = "UPDATE MEASUREMENTS SET COLUMNS = '0' WHERE NAME = 'C" + j + "'";
            db.execSQL(query);
        }
    }

//    Parses all the measurements in a cell to find a specific AP
    public boolean checkAPExists(String access_point, String cell) {
        SQLiteDatabase db = this.getReadableDatabase();
        String st = "select * from " + cell + " where " + AP + " = '" + access_point + "'";
        Cursor data = db.rawQuery(st, null);
        return data.moveToNext();
    }

//    Adds AP id (if it does not exist) and RSS signal values to chosen measurement
    public boolean addData(String ar, int signal_strength, String cell, String cur_column) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!checkAPExists(ar, cell)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AP, ar);
            contentValues.put(cur_column, signal_strength);
            long result = db.insert(cell, null, contentValues);
            return result != -1;
        } else {
            String query = "UPDATE " + cell + " SET " + cur_column + " = '" + signal_strength + "' WHERE " + AP + " = '" + ar + "'";
            db.execSQL(query);
        }
        return true;
    }

//    Checks if a value in a table is null value
    public boolean isNull(String access_point, String cell, String measurement) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + measurement + " FROM " + cell + " WHERE " + AP + " = '" + access_point + "'", null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.isNull(0);
        }
        return false;
    }

//    Returns the number of measurements made in a cell. If none, it returns 0.
    public int getPopulatedColumns(String cell){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COLUMNS FROM MEASUREMENTS WHERE NAME = '" + cell + "'", null);
        if (cursor != null && cursor.moveToFirst()) {
            if (!cursor.isNull(0)){
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public void increaseColumnCount(String cell){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COLUMNS FROM MEASUREMENTS WHERE NAME = '" + cell + "'", null);
        if (cursor != null && cursor.moveToFirst()) {
            if (!cursor.isNull(0)){
                int index = cursor.getInt(0) + 1;
                if (index<40){
                    String query = "UPDATE MEASUREMENTS SET COLUMNS = '" + index + "' WHERE NAME = '" + cell + "'";
                    db.execSQL(query);
                }
            }
            else {
                String query = "UPDATE MEASUREMENTS SET COLUMNS = '1' WHERE NAME = '" + cell + "'";
                db.execSQL(query);
            }
        }
    }
}

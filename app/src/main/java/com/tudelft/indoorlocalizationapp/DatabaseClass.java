package com.tudelft.indoorlocalizationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseClass extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseClass";

    private static final String TABLE_NAME = "knn_signals";
    private static final String COL1 = "ID";
    private static final String COL2 = "access_points";
    private static final String COL3 = "c1";
    private static final String COL4 = "c2";
    private static final String COL5 = "c3";
    private static final String COL6 = "c4";


    public DatabaseClass(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 + " TEXT, " +
                COL3 + " INTEGER, " + COL4 + " INTEGER, " + COL5 + " INTEGER, " + COL6 + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean CheckAPExists(int access_point) {
        boolean nameExists = false;

        SQLiteDatabase db = this.getReadableDatabase();
        String st = "select * from " + TABLE_NAME + " where " + COL2 + " = '" + access_point + "'";
        Cursor data = db.rawQuery(st, null);
        if(data.moveToNext())
        {
            nameExists = true;
        }
        return nameExists;
    }

    public boolean newData(String cell_column, String access_point, int signal_strength){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, access_point);
        contentValues.put(cell_column, signal_strength);

        Log.d(TAG, "addData: Adding " + signal_strength + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public void addData(String cell_column, String access_point, int signal_strength) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE " + TABLE_NAME + " SET " + cell_column + " = '" + signal_strength + "' WHERE " + COL2 + " = '" + access_point + "'";
        db.execSQL(query);
    }
}

package com.nikhil.nosqldb.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nikhil.nosqldb.DataBase;

import java.util.ArrayList;

/**
 * Created by Nikhil on 4/29/2017.
 */

public class Utility {
    public static boolean isOSExist(DataBase dataBase, String osName) {
        SQLiteDatabase sqLiteDatabase = dataBase.getReadableDatabase();
        Cursor dbCursor = sqLiteDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + osName + "'", null);
        if (dbCursor != null) {
            if (dbCursor.getCount() > 0) {
                dbCursor.close();
                return true;
            }
            dbCursor.close();
        }
        return false;
    }

    public static ArrayList<String> getOSCols(DataBase mDataBase, String mOSName) {
        SQLiteDatabase sqLiteDatabase = mDataBase.getReadableDatabase();
        Cursor dbCursor = sqLiteDatabase.query(mOSName, null, null, null, null, null, null);
        String[] cols = dbCursor.getColumnNames();
        dbCursor.close();
        return Utility.toArrayList(cols);
    }

    public static ArrayList<String> toArrayList(String[] dataArray) {
        ArrayList<String> dataList = new ArrayList<>();
        for (String col :
                dataArray) {
            dataList.add(col);
        }
        return dataList;
    }
}

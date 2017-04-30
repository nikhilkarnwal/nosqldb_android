package com.nikhil.nosqldb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Main class to create and alter database
 *
 * @author Nikhil
 */
public class DataBase {
    private final int DB_Version = 1;
    private boolean IsOpen = false;
    private SQLiteOpenHelper mSQLiteHelper;
    private String mDBName;

    /**
     * Create or Open database
     *
     * @param dbName  Name of the Database
     * @param context
     */
    public DataBase(String dbName, Context context) {
        mDBName = dbName;
        mSQLiteHelper = new SQLiteOpenHelper(context, dbName, null, DB_Version) {

            @Override
            public void onCreate(SQLiteDatabase db) {
                DataBase.this.onCreate();
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                DataBase.this.onUpgrade(oldVersion, newVersion);
            }

            @Override
            public void onOpen(SQLiteDatabase db) {
                DataBase.this.onOpen();
            }
        };
        mSQLiteHelper.getReadableDatabase();
    }

    /**
     * Create or Open Database.
     * Call this method to open database explicitly.
     */
    public void openDataBase() {
        mSQLiteHelper.getReadableDatabase();
    }

    /**
     * Callback method on database opening.
     * <p>
     * Override this method for using it as callback
     * but don't create ObjectStore under this.
     */
    protected void onOpen() {
        IsOpen = true;
    }

    /**
     * Callback method on database creation.
     * <p>
     * Override this method for using it as callback
     * but don't create ObjectStore under this.
     */
    protected void onCreate() {
        IsOpen = true;
    }

    /**
     * Callback method on database upgrade
     * <p>
     * Override this method for using it as callback
     * but don't create ObjectStore under this.
     */
    protected void onUpgrade(int oldVersion, int newVersion) {
        IsOpen = true;
    }

    /**
     * @return SQLiteDatabase for read/write
     */
    public SQLiteDatabase getWritableDatabase() {
        return mSQLiteHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase() {
        return mSQLiteHelper.getReadableDatabase();
    }

    /**
     * Create ObjectStore instance.
     * <p>
     * It doesn't create ObjectStore in database.
     *
     * @param osName Name of ObjectStore
     * @return instance of {@link ObjectStore}
     */
    public ObjectStore createObjectStore(String osName) {
        return new ObjectStore(osName, this);
    }

    /**
     * Close any open database object
     */
    public void close() {
        mSQLiteHelper.close();
    }

    /**
     * Returns true if the database is currently open.
     *
     * @return True if database is currently open
     */
    public boolean isOpen() {
        return mSQLiteHelper.getReadableDatabase().isOpen();
    }
}

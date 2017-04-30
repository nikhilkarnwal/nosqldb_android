package com.nikhil.nosqldb;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.nikhil.nosqldb.constant.Constant;
import com.nikhil.nosqldb.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class provides random read-write access to the result set returned
 * by a database query.
 * <p>
 * OSCursor are not required to be synchronized so code using a OSCursor from multiple
 * threads should perform its own synchronization when using the OSCursor.
 * </p>
 *
 * @author Nikhil
 */
public class OSCursor {
    private Cursor mCursor;
    private HashMap<String, String> mRowData;

    public OSCursor(Cursor cursor) {
        mCursor = cursor;
        mRowData = new HashMap<>(mCursor.getColumnCount());
    }

    /**
     * Returns the numbers of rows in the cursor.
     *
     * @return the number of rows in the cursor.
     */
    public int getCount() {
        return mCursor.getCount();
    }

    /**
     * Returns the current position of the cursor in the row set.
     * The value is zero-based. When the row set is first returned the cursor
     * will be at positon -1, which is before the first row. After the
     * last row is returned another call to next() will leave the cursor past
     * the last entry, at a position of count().
     *
     * @return the current cursor position.
     */
    public int getPosition() {
        return mCursor.getPosition();
    }

    private boolean move(int offset) {
        return mCursor.move(offset);
    }

    private boolean moveToPosition(int position) {
        return mCursor.moveToPosition(position);
    }

    /**
     * Move the cursor to the first row, fetch it's content and cache it
     * <p>
     * <p>This method will return false if the cursor is empty.</p>
     *
     * @return whether the move succeeded.
     */
    public boolean moveToFirst() {
        boolean result = mCursor.moveToFirst();
        if (result) {
            fetchRow();
        }
        return result;
    }

    /**
     * Move the cursor to the last row, fetch it's content and cache it
     * <p>
     * <p>This method will return false if the cursor is empty.</p>
     *
     * @return whether the move succeeded.
     */
    public boolean moveToLast() {
        boolean result = mCursor.moveToLast();
        if (result) {
            fetchRow();
        }
        return result;
    }

    /**
     * Move the cursor to the next row, fetch it's content and cache it
     * <p>
     * <p>This method will return false if the cursor is empty.</p>
     *
     * @return whether the move succeeded.
     */
    public boolean moveToNext() {
        boolean result = mCursor.moveToNext();
        if (result) {
            fetchRow();
        }
        return result;
    }

    /**
     * Move the cursor to the previous row, fetch it's content and cache it
     * <p>
     * <p>This method will return false if the cursor is empty.</p>
     *
     * @return whether the move succeeded.
     */
    public boolean moveToPrevious() {
        boolean result = mCursor.moveToPrevious();
        if (result) {
            fetchRow();
        }
        return result;
    }

    /**
     * Return if cursor is at first index
     *
     * @return True if cursor is at first index
     */
    public boolean isFirst() {
        return mCursor.isFirst();
    }

    /**
     * Return if cursor is at last index
     *
     * @return True if cursor is at last index
     */
    public boolean isLast() {
        return mCursor.isLast();
    }

    /**
     * Return if cursor is at -1 index
     *
     * @return True if cursor is at -1 index
     */
    public boolean isBeforeFirst() {
        return mCursor.isBeforeFirst();
    }

    /**
     * Return if cursor is at count index
     *
     * @return True if cursor is at count index
     */
    public boolean isAfterLast() {
        return mCursor.isAfterLast();
    }

    /**
     * Get all columns in that particular row, columns may vary
     * for different rows as it is no sqldb
     *
     * @return Array of columns in current row
     */
    public String[] getColumnNames() {
        ArrayList<String> cols = Utility.toArrayList(mCursor.getColumnNames());
        String extraData = mCursor.getString(mCursor.getColumnIndex(Constant.EXTRA_DATA_COLUMN));
        if (!TextUtils.isEmpty(extraData)) {
            HashMap<String, String> data = new Gson().fromJson(extraData, HashMap.class);
            cols.addAll(data.keySet());
        }
        String[] colsArray = new String[cols.size()];
        return cols.toArray(colsArray);
    }

    /**
     * Return number of columns in current row
     *
     * @return number of columns in current row
     */
    public int getColumnCount() {
        return getColumnNames().length;
    }

    public byte[] getBlob(int columnIndex) {
        return new byte[0];
    }

    private void fetchRow() {
        mRowData.clear();

        String[] cols = mCursor.getColumnNames();
        for (String col :
                cols) {
            String value = mCursor.getString(mCursor.getColumnIndex(col));
            if (!TextUtils.isEmpty(value)) {
                if (!col.equalsIgnoreCase(Constant.EXTRA_DATA_COLUMN)) {
                    mRowData.put(col, value);
                } else {
                    mRowData.putAll(new Gson().fromJson(value, HashMap.class));
                }
            }
        }
    }

    /**
     * Return current row as HashMap of column, value as key, value
     *
     * @return current row as HashMap of column, value as key, value
     */
    public HashMap<String, String> getRow() {
        return mRowData;
    }

    /**
     * Return value of specified column in current row,
     * null if there is no value for the column
     *
     * @param columnKey Column Name
     * @return value of specified column in current row
     */
    public String getValue(String columnKey) {
        if (mRowData.containsKey(columnKey))
            return mRowData.get(columnKey);
        return null;
    }

    private void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {

    }

    /**
     * Closes the Cursor, releasing all of its resources and making it completely invalid.
     */
    public void close() {
        mCursor.close();
    }

    /**
     * Return true if cursor is closed
     *
     * @return true if cursor is closed
     */
    public boolean isClosed() {
        return mCursor.isClosed();
    }

    private void registerContentObserver(ContentObserver observer) {

    }

    private void unregisterContentObserver(ContentObserver observer) {

    }

    private void registerDataSetObserver(DataSetObserver observer) {

    }

    private void unregisterDataSetObserver(DataSetObserver observer) {

    }

    private void setNotificationUri(ContentResolver cr, Uri uri) {

    }

    private Uri getNotificationUri() {
        return null;
    }

    private boolean getWantsAllOnMoveCalls() {
        return false;
    }

    private void setExtras(Bundle extras) {

    }

    private Bundle getExtras() {
        return null;
    }

    private Bundle respond(Bundle extras) {
        return null;
    }
}

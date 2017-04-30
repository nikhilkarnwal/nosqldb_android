package com.nikhil.nosqldb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.nikhil.nosqldb.constant.Constant;
import com.nikhil.nosqldb.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.nikhil.nosqldb.constant.Constant.EXTRA_DATA_COLUMN;

/**
 * Class to control read/write operation of ObjectStore on database
 *
 * @author Nikhil
 */

public class ObjectStore {
    private DataBase mDataBase = null;
    private ConcurrentHashMap<String, List<String>> mIndexCols = new ConcurrentHashMap<>();
    private HashSet<String> mCols = new HashSet<>();
    private String mOSName;
    private boolean mIsReadOnly = false;

    /**
     * @param osName Name of ObjectStore in database
     */
    public ObjectStore(String osName) {
        mOSName = osName;
        mCols.add(EXTRA_DATA_COLUMN);
    }

    /**
     * @param osName   Name of ObjectStore in database
     * @param dataBase Instance of database in which this objectstore will be created
     */
    public ObjectStore(String osName, DataBase dataBase) {
        mOSName = osName;
        mDataBase = dataBase;
        mCols.add(EXTRA_DATA_COLUMN);
    }

    /**
     * It will create/open objectstore.
     * <p>
     * Call this once {@link DataBase} is open
     */
    public void init() {
        if (Utility.isOSExist(mDataBase, mOSName)) {
            mCols.addAll(Utility.getOSCols(mDataBase, mOSName));
        } else {
            try {
                createOS();
            } catch (Exception e) {
                Log.v("Table Creation", e.getMessage());
            }
        }
    }

    /**
     * It will create ObjectStore by creating table and
     * required indexes
     *
     * @throws Exception
     */
    private void createOS() throws Exception {
        StringBuilder create_os_cmd = new StringBuilder("CREATE TABLE IF NOT EXISTS " + mOSName + " (");
        create_os_cmd.append(Constant.ROW_ID_COLUMN).append(" INTEGER PRIMARY KEY");
        for (String col :
                mCols) {
            create_os_cmd.append("," + col).append(" TEXT");
        }
        create_os_cmd.append(")");
        executeSQL(create_os_cmd.toString());
        for (String key :
                mIndexCols.keySet()) {
            String create_index_cmd = "CREATE UNIQUE INDEX IF NOT EXISTS " + key +
                    " ON " + mOSName + " (" + TextUtils.join(",", mIndexCols.get(key)) +
                    " )";
            executeSQL(create_index_cmd);
        }
    }

    /**
     * Execute SQLite command
     *
     * @param sqlCMD SQLite command to run
     * @throws Exception
     */
    private void executeSQL(String sqlCMD) throws Exception {
        if (mIsReadOnly) {
            throw new Exception("Trying to write on readonly database");
        }
        Log.v("Execute SQL", sqlCMD);
        SQLiteDatabase sqLiteDatabase = mDataBase.getWritableDatabase();
        sqLiteDatabase.execSQL(sqlCMD);
    }

    /**
     * Put data in database
     *
     * @param contentValues Data to store
     * @throws Exception
     */
    private void put(ContentValues contentValues) throws Exception {
        if (mIsReadOnly) {
            throw new Exception("Trying to write on readonly database");
        }
        SQLiteDatabase sqLiteDatabase = mDataBase.getWritableDatabase();
        sqLiteDatabase.insert(mOSName, null, contentValues);
    }

    /**
     * Create index on current ObjectStore.
     * <p>
     * <p>
     * Create all indexes before {@link #init()}
     * </P>
     *
     * @param indexKey  Name of index
     * @param indexCols Set of columns to create index on
     */
    public void createIndex(String indexKey, ArrayList<String> indexCols) {
        mIndexCols.put(indexKey, indexCols);
        mCols.addAll(indexCols);
    }

    /**
     * Open this ObjectStore in readonly mode
     *
     * @param isReadOnly
     */
    public void setReadOnlyMode(boolean isReadOnly) {
        mIsReadOnly = isReadOnly;
    }

    /**
     * Create this ObjectStore in specified database.
     * <p>
     * <p class="caution">
     * Call {@link #init()} after this function to create the objectstore on this database.
     * </p>
     *
     * @param dataBase DataBase in which this objectstore will be created
     */
    public void setDataBase(DataBase dataBase) {
        mDataBase = dataBase;
    }

    /**
     * Add entry/row into the objectstore
     *
     * @param values row to be added
     * @throws Exception
     */
    public void put(HashMap<String, String> values) throws Exception {
        ContentValues contentValues = new ContentValues();
        for (Object key :
                values.keySet().toArray()) {
            if (mCols.contains((String) key)) {
                contentValues.put((String) key, values.get(key));
                values.remove(key);
            }
        }
        if (!values.isEmpty()) {
            String extraData = new Gson().toJson(values);
            contentValues.put(EXTRA_DATA_COLUMN, extraData);
        }
        put(contentValues);
    }

    /**
     * Return cursor for the objectstore.
     * <p>It will be pointing at the start of the objectstore</p>
     *
     * @return Cursor at the start of the objectstore
     */
    public OSCursor getCursor() {
        SQLiteDatabase database = mDataBase.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + mOSName, null);
        if (cursor != null) {
            return new OSCursor(cursor);
        }
        return null;
    }

    /**
     * Return cursor pointing to start of row fetched according to the query
     *
     * @param selection     A filter declaring which rows to return, formatted as an
     *                      SQL WHERE clause (excluding the WHERE itself). Passing null
     *                      will return all rows for the given table.
     *                      <p>
     *                      Use ? to pass value from selectionArgs
     *                      </p>
     *                      <p>
     *                      e.g.     "name = ?"
     *                      </p>
     *                      <p>
     *                      Make sure condition is on indexed columns only, indexed via {@link #createIndex(String, ArrayList)}
     *                      </p>
     * @param selectionArgs You may include ?s in selection, which will be
     *                      replaced by the values from selectionArgs, in order that they
     *                      appear in the selection. The values will be bound as Strings.
     *                      <p>
     *                      e.g. "nikhil"
     *                      </p>
     * @param groupBy       A filter declaring how to group rows, formatted as an SQL
     *                      GROUP BY clause (excluding the GROUP BY itself). Passing null
     *                      will cause the rows to not be grouped.
     *                      <p>
     *                      e.g. col_1, col_2,...
     *                      </p>
     * @param having        A filter declare which row groups to include in the cursor,
     *                      if row grouping is being used, formatted as an SQL HAVING
     *                      clause (excluding the HAVING itself). Passing null will cause
     *                      all row groups to be included, and is required when row
     *                      grouping is not being used.
     *                      <p>
     *                      e.g. col_1 = "xyz"
     *                      </p>
     * @param orderBy       How to order the rows, formatted as an SQL ORDER BY clause
     *                      (excluding the ORDER BY itself). Passing null will use the
     *                      default sort order, which may be unordered.
     * @param limit         Limits the number of rows returned by the query,
     *                      formatted as LIMIT clause. Passing null denotes no LIMIT clause.
     * @return A {@link OSCursor} object, which is positioned before the first entry. Note that
     * {@link OSCursor}s are not synchronized, see the documentation for more details.
     * @see Cursor
     * @see OSCursor
     */
    public OSCursor query(String selection, String[] selectionArgs,
                          String groupBy, String having, String orderBy,
                          String limit) {
        SQLiteDatabase database = mDataBase.getReadableDatabase();
        Cursor cursor = database.
                query(mOSName, null,
                        selection, selectionArgs,
                        groupBy, having, orderBy,
                        limit);
        if (cursor != null) {
            return new OSCursor(cursor);
        }
        return null;
    }

    /**
     * Return cursor pointing to start of row fetched according to the query
     *
     * @param selection     A filter declaring which rows to return, formatted as an
     *                      SQL WHERE clause (excluding the WHERE itself). Passing null
     *                      will return all rows for the given table.
     *                      <p>
     *                      Use ? to pass value from selectionArgs
     *                      </p>
     *                      <p>
     *                      e.g.     "name = ?"
     *                      </p>
     *                      <p>
     *                      Make sure condition is on indexed columns only, indexed via {@link #createIndex(String, ArrayList)}
     *                      </p>
     * @param selectionArgs You may include ?s in selection, which will be
     *                      replaced by the values from selectionArgs, in order that they
     *                      appear in the selection. The values will be bound as Strings.
     *                      <p>
     *                      e.g. "nikhil"
     *                      </p>
     * @return A {@link OSCursor} object, which is positioned before the first entry. Note that
     * {@link OSCursor}s are not synchronized, see the documentation for more details.
     * @see Cursor
     * @see OSCursor
     */
    public OSCursor query(String selection, String[] selectionArgs) {
        return query(selection, selectionArgs,
                null, null, null,
                null);
    }
}

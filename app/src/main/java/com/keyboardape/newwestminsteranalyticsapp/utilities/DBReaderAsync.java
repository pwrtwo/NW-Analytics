package com.keyboardape.newwestminsteranalyticsapp.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Database Reader Async Task.
 */
public class DBReaderAsync extends AsyncTask<Void, Void, Void> {

    private Callbacks   mCallbacks;
    private String      mSQLQuery;

    public DBReaderAsync(Callbacks callbacks, String sqlQuery) {
        mCallbacks   = callbacks;
        mSQLQuery    = sqlQuery;
    }

    // ---------------------------------------------------------------------------------------------
    //                                          ASYNC TASK
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... aVoid) {
        SQLiteDatabase db = DBHelper.GetInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(mSQLQuery, null);
        mCallbacks.onDBCursorReady(cursor);
        cursor.close();
        db.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mCallbacks.onDBReadComplete();
    }

    // ---------------------------------------------------------------------------------------------
    //                       CALLBACK : ON DB CURSOR READY, ON DB READ COMPLETE
    // ---------------------------------------------------------------------------------------------

    public interface Callbacks {
        void onDBCursorReady(Cursor cursor);
        void onDBReadComplete();
    }
}
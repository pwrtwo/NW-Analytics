package com.keyboardape.newwestminsteranalyticsapp.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetTracker;

/**
 * Singleton Data Manager shared across all Android Activities.
 */
public final class DBHelper extends SQLiteOpenHelper {

    // ---------------------------------------------------------------------------------------------
    //                                   STATIC : INITIALIZATION
    // ---------------------------------------------------------------------------------------------

    private final static String DB_NAME = "datasets.sqlite";
    private final static int    DB_VERSION = 1;

    private static DBHelper     DBHelperInstance = null;
    private static boolean      IsActivityStopped = false;

    public static synchronized void Initialize(Context context) {
        if (DBHelperInstance == null || IsActivityStopped) {
            IsActivityStopped = false;
            DBHelperInstance = new DBHelper(context.getApplicationContext());
        }
    }

    public static void SetActivityStopped() {
        IsActivityStopped = true;
    }

    public static DBHelper GetInstance() {
        return DBHelperInstance;
    }

    // ---------------------------------------------------------------------------------------------
    //                                     INSTANCE: DB HELPER
    // ---------------------------------------------------------------------------------------------

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 1) {
                db.execSQL(DataSetTracker.GetQueryForTableCreation());
            }
        } catch (Exception e) {
            Log.e(DBHelper.class.getSimpleName(), e.getMessage());
        }
    }
}

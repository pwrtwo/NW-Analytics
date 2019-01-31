package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.utilities.CSVParserAsync;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Age Demographics DataSet.
 *
 * Downloaded from Stats Canada Census:
 * http://www12.statcan.gc.ca/census-recensement/index-eng.cfm
 *
 * 2016 csv - http://keyboardape.com/2016newwestanalytics/datasets/2016_age_demographics.CSV
 *
 * 2011 csv - http://keyboardape.com/2016newwestanalytics/datasets/2011_age_demographics.CSV
 *
 * 2006 csv - http://keyboardape.com/2016newwestanalytics/datasets/2006_community_profile.CSV
 *
 * 2001 csv - http://keyboardape.com/2016newwestanalytics/datasets/2001_age_demographics.CSV
 */
public class AgeDemographicsData extends DataSet implements CSVParserAsync.Callbacks {

    public static final int[]                CENSUS_DATA_YEARS;

    private final static String              TABLE_NAME;
    private final static String[]            DATA_SOURCE_URLS;
    private final static DataSetType         DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;
    private final static int                 R_STRING_ID_NAME;
    private final static int                 MAX_AGE;

    static {
        CENSUS_DATA_YEARS = new int[] {2016, 2011, 2006, 2001};

        TABLE_NAME       = "age_demographics";
        DATA_SET_TYPE    = DataSetType.AGE_DEMOGRAPHICS;
        R_STRING_ID_NAME = R.string.dataset_age_demographics;
        MAX_AGE          = 300;

        // Order declared here must match switch in function onNewCSVRowFromStream()
        DATA_SOURCE_URLS = new String[] {
                "http://keyboardape.com/2016newwestanalytics/datasets/2001_age_demographics.CSV"    // 2001
                ,"http://keyboardape.com/2016newwestanalytics/datasets/2006_community_profile.CSV"  // 2006
                ,"http://keyboardape.com/2016newwestanalytics/datasets/2011_age_demographics.CSV"   // 2011
                ,"http://keyboardape.com/2016newwestanalytics/datasets/2016_age_demographics.CSV"}; // 2016

        TABLE_COLUMNS = new HashMap<>();
        TABLE_COLUMNS.put("ID",                "INTEGER PRIMARY KEY AUTOINCREMENT");
        // Original Data
        TABLE_COLUMNS.put("YEAR",              "INTEGER"); // e.g. 2016
        TABLE_COLUMNS.put("MINAGE",            "INTEGER"); // e.g. 0
        TABLE_COLUMNS.put("MAXAGE",            "INTEGER"); // e.g. 4
        TABLE_COLUMNS.put("MALE_POPULATION",   "INTEGER"); // e.g. 1115
        TABLE_COLUMNS.put("FEMALE_POPULATION", "INTEGER"); // e.g. 1115
    }

    private SQLiteDatabase                   mDB;
    private OnDataSetUpdatedCallbackInternal mCallback;
    private int                              mCurrentDataSource;

    // Used only in parsing 2001 DataSet
    private int m2001TrackingDataGroup = 0;

    public AgeDemographicsData() {
        super(DATA_SOURCE_URLS[3], DATA_SET_TYPE ,TABLE_NAME ,TABLE_COLUMNS, R_STRING_ID_NAME);
        mDB = null;
        mCallback = null;
        mCurrentDataSource = 0;
    }

    // ---------------------------------------------------------------------------------------------
    //                                         DOWNLOAD DATA
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void downloadDataToDBAsync(final OnDataSetUpdatedCallbackInternal callback) {
        if (mCurrentDataSource == 0) {
            mDB = DBHelper.GetInstance().getWritableDatabase();
            mCallback = callback;
        }
        new CSVParserAsync(this, DATA_SOURCE_URLS[mCurrentDataSource]).execute();
    }

    @Override
    public void onNewCSVRowFromStream(String[] row) {
        try {
            switch (mCurrentDataSource) {
                case 0:
                    saveDataFrom2001(row);
                    break;
                case 1:
                    saveDataFrom2006(row);
                    break;
                case 2:
                    saveDataFrom2011Or2016(row, 2011);
                    break;
                case 3:
                    saveDataFrom2011Or2016(row, 2016);
                    break;
                default:
                    Log.e(AgeDemographicsData.class.getSimpleName(), "Data source out of bounds.");
                    break;
            }
        } catch (Exception e) {
            Log.e(AgeDemographicsData.class.getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void onCSVStreamParsed(boolean isSuccessful, long dataLastUpdated) {
        ++mCurrentDataSource;
        if (mCurrentDataSource >= DATA_SOURCE_URLS.length) {
            mDB.close();
            mCallback.onDataSetUpdated(DATA_SET_TYPE, isSuccessful, dataLastUpdated);
        } else {
            downloadDataToDBAsync(mCallback);
        }
    }

    private void saveDataFrom2011Or2016(String[] row, int year) {
        if (row.length != 4
            || !row[0].contains("years")) {
            return;
        }

        row[0] = row[0].substring(1, row[0].length() - 1).replace("to", "").replace("years", "").trim();
        String[] ageRange = row[0].split("[ ]+");
        Integer minAge;
        Integer maxAge;
        if (ageRange.length == 2) {
            minAge = ParseToIntOrNull(ageRange[0]);
            maxAge = ParseToIntOrNull(ageRange[1]);
            if (minAge == null
                || maxAge == null
                || maxAge - minAge > 5) {
                return;
            }
        } else if (ageRange.length == 3) {
            if (!row[0].contains("100")) {
                return;
            }
            minAge = 100;
            maxAge = MAX_AGE;
        } else {
            return;
        }

        ContentValues c = new ContentValues();
        c.put("YEAR",              year);
        c.put("MINAGE",            minAge);
        c.put("MAXAGE",            maxAge);
        c.put("MALE_POPULATION",   ParseToIntOrNull(row[2]));
        c.put("FEMALE_POPULATION", ParseToIntOrNull(row[3].trim()));
        mDB.insert(TABLE_NAME, null, c);
    }

    private void saveDataFrom2006(String[] row) {
        if (row.length != 8
            || !row[0].equals("Age characteristics")
            || !row[1].contains("years")) {
            return;
        }

        row[1] = row[1].replace("to", "").replace("years", "").trim();
        String[] ageRange = row[1].split("[ ]+");
        Integer minAge = ParseToIntOrNull(ageRange[0]);
        Integer maxAge = ParseToIntOrNull(ageRange[1]);
        if (maxAge == null) {
            maxAge = MAX_AGE;
        }

        ContentValues c = new ContentValues();
        c.put("YEAR",              2006);
        c.put("MINAGE",            minAge);
        c.put("MAXAGE",            maxAge);
        c.put("MALE_POPULATION",   ParseToIntOrNull(row[3]));
        c.put("FEMALE_POPULATION", ParseToIntOrNull(row[4]));
        mDB.insert(TABLE_NAME, null, c);
    }

    private void saveDataFrom2001(String[] row) {
        if (row[0].contains("Male")) {
            m2001TrackingDataGroup = 1;
            return;
        } else if (row[0].contains("Female")) {
            m2001TrackingDataGroup = 2;
            return;
        } else if (row[0].contains("Note")) {
            m2001TrackingDataGroup = 0;
            return;
        } else if (m2001TrackingDataGroup == 0) {
            return;
        }

        row[0] = row[0].substring(1, row[0].length() - 1).trim();
        String[] ageRange = row[0].split("-");
        Integer minAge;
        Integer maxAge;
        if (ageRange.length == 2) {
            minAge = ParseToIntOrNull(ageRange[0]);
            maxAge = ParseToIntOrNull(ageRange[1]);
        } else {
            minAge = ParseToIntOrNull(row[0].replace("+", ""));
            maxAge = MAX_AGE;
        }

        ContentValues c = new ContentValues();

        // male group
        if (m2001TrackingDataGroup == 1) {
            c.put("YEAR",              2001);
            c.put("MINAGE",            minAge);
            c.put("MAXAGE",            maxAge);
            c.put("MALE_POPULATION",   ParseToIntOrNull(row[1]));
            mDB.insert(TABLE_NAME, null, c);
        }

        // female group
        else if (m2001TrackingDataGroup == 2) {
            c.put("FEMALE_POPULATION", ParseToIntOrNull(row[1]));
            String minAgeSpecifier = (minAge == null) ? "MINAGE IS NULL" : "MINAGE = " + minAge;
            String maxAgeSpecifier = (maxAge == null) ? "MAXAGE IS NULL" : "MAXAGE = " + maxAge;
            mDB.update(TABLE_NAME,
                    c,
                    "YEAR = " + 2001 + " AND " + minAgeSpecifier + " AND " + maxAgeSpecifier,
                    null);
        }
    }
}

package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;
import com.keyboardape.newwestminsteranalyticsapp.utilities.JSONParserAsync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Building Age DataSet.
 *
 * Downloaded from New West Open Data:
 * http://opendata.newwestcity.ca/datasets/building-age
 */
public class BuildingAgeData extends DataSet {

    private final static String              TABLE_NAME;
    private final static String              DATA_SOURCE_URL;
    private final static DataSetType         DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;
    private final static int                 R_STRING_ID_NAME;

    static {
        TABLE_NAME       = "building_age";
        DATA_SOURCE_URL  = "http://opendata.newwestcity.ca/downloads/building-age/BUILDING_AGE.json";
        DATA_SET_TYPE    = DataSetType.BUILDING_AGE;
        R_STRING_ID_NAME = R.string.dataset_building_age;

        TABLE_COLUMNS = new HashMap<>();
        TABLE_COLUMNS.put("ID",        "INTEGER PRIMARY KEY AUTOINCREMENT");
        // Original Data
        TABLE_COLUMNS.put("STRNUM",    "TEXT");    // e.g. "115"
        TABLE_COLUMNS.put("STRNAM",    "TEXT");    // e.g. "SECOND ST"
        TABLE_COLUMNS.put("BLDGNAM",   "TEXT");    // e.g. "QUEEN'S COURT"
        TABLE_COLUMNS.put("DEVELOPER", "TEXT");    // e.g. "E.J. FADER"
        TABLE_COLUMNS.put("ARCHITECT", "TEXT");    // e.g. "SAIT, E.G.W."
        TABLE_COLUMNS.put("BLDG_ID",   "INTEGER"); // e.g. 291
        TABLE_COLUMNS.put("MAPREF",    "INTEGER"); // e.g. 847000
        TABLE_COLUMNS.put("UNITNUM",   "INTEGER"); // e.g. null
        TABLE_COLUMNS.put("BLDGAGE",   "INTEGER"); // e.g. 1971
        TABLE_COLUMNS.put("MOVED",     "INTEGER"); // e.g. 0
        TABLE_COLUMNS.put("LATITUDE",  "REAL");    // e.g.   49.20975021764765
        TABLE_COLUMNS.put("LONGITUDE", "REAL");    // e.g. -122.90530144621799
    }

    public BuildingAgeData() {
        super(DATA_SOURCE_URL, DATA_SET_TYPE ,TABLE_NAME ,TABLE_COLUMNS, R_STRING_ID_NAME);
    }

    @Override
    protected void downloadDataToDBAsync(final OnDataSetUpdatedCallbackInternal callback) {
        final SQLiteDatabase db = DBHelper.GetInstance().getWritableDatabase();
        new JSONParserAsync(new JSONParserAsync.Callbacks() {
            @Override
            public void onNewJsonObjectFromStream(JSONObject o) {
                try {
                    ContentValues c = new ContentValues();

                    // Original Data
                    Integer buildingAge = ParseToIntOrNull(o.getString("BLDGAGE"));
                    if (buildingAge != null && buildingAge == 0) {
                        buildingAge = null;
                    }

                    c.put("STRNUM",    ParseToStringOrNull(o.getString("STRNUM")));
                    c.put("STRNAM",    ParseToStringOrNull(o.getString("STRNAM")));
                    c.put("BLDGNAM",   ParseToStringOrNull(o.getString("BLDGNAM")));
                    c.put("DEVELOPER", ParseToStringOrNull(o.getString("DEVELOPER")));
                    c.put("ARCHITECT", ParseToStringOrNull(o.getString("ARCHITECT")));
                    c.put("BLDG_ID",   ParseToIntOrNull(o.getString("BLDG_ID")));
                    c.put("MAPREF",    ParseToIntOrNull(o.getString("MAPREF")));
                    c.put("UNITNUM",   ParseToIntOrNull(o.getString("UNITNUM")));
                    c.put("BLDGAGE",   buildingAge);
                    c.put("MOVED",     ParseToIntOrNull(o.getString("MOVED")));

                    JSONArray coordinates = GetAverageCoordinatesFromJsonGeometryOrNull(o);
                    c.put("LATITUDE", (coordinates == null) ? null : coordinates.getDouble(1));
                    c.put("LONGITUDE",(coordinates == null) ? null : coordinates.getDouble(0));

                    db.insert(TABLE_NAME, null, c);
                } catch (Exception e) {
                    Log.e(BuildingAgeData.class.getSimpleName(), e.getMessage() + "::" + o.toString());
                }
            }
            @Override
            public void onJsonStreamParsed(boolean isSuccessful, long dataLastUpdated) {
                db.close();
                callback.onDataSetUpdated(DATA_SET_TYPE, isSuccessful, dataLastUpdated);
            }
        }, DATA_SOURCE_URL).execute();
    }
}

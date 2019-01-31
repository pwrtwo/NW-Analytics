package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.maplayerinfo.BuildingAgeFragment;
import com.keyboardape.newwestminsteranalyticsapp.maplayerinfo.MapLayerInfoFragment;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Building Age MapLayer.
 */
public class BuildingAgeLayer extends MapLayer {

    // ---------------------------------------------------------------------------------------------
    //                                             STATIC
    // ---------------------------------------------------------------------------------------------

    /** Android resource id: layer name. */
    private final static int          R_STRING_ID_LAYER_NAME;

    /** Android resource id: layer icon. */
    private final static int          R_DRAWABLE_ID_ICON;

    /** MapLayerType. */
    private final static MapLayerType LAYER_TYPE;

    /** TileOverlay heatmap spread radius. */
    private final static int          HEATMAP_RADIUS;

    /** Tracks whether this MapLayer has selected area functions. */
    private final static boolean      HAS_SELECTED_AREA_FUNCTIONS;

    // Used to reduce the intensity of older buildings to give values
    // a better visual representation of data
    private final static int          YEAR_REDUCTION;

    static {
        R_STRING_ID_LAYER_NAME      = R.string.layer_building_age;
        R_DRAWABLE_ID_ICON          = R.drawable.ic_account_balance_black_24dp;
        LAYER_TYPE                  = MapLayerType.BUILDING_AGE;
        HEATMAP_RADIUS              = 10;
        HAS_SELECTED_AREA_FUNCTIONS = true;

        YEAR_REDUCTION              = 10;
    }

    // ---------------------------------------------------------------------------------------------
    //                                           INSTANCE
    // ---------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public BuildingAgeLayer() {
        super(LAYER_TYPE, R_STRING_ID_LAYER_NAME, R_DRAWABLE_ID_ICON, HEATMAP_RADIUS, HAS_SELECTED_AREA_FUNCTIONS);
    }

    /**
     * Gets MapLayer data asynchronously.
     * @param callback function when data is ready
     */
    @Override
    public void getMapDataAsync(final OnMapLayerDataReadyCallback callback) {
        int yearReduction = getAggregate("MAX(BLDGAGE) - " + YEAR_REDUCTION);
        String buildingAgeTableName = DataSetType.BUILDING_AGE.getDataSet().getTableName();
        String sqlQuery = "SELECT LATITUDE, LONGITUDE, BLDGAGE "
                + "FROM " + buildingAgeTableName + " "
                + "WHERE BLDGAGE IS NOT NULL "
                + "AND LATITUDE IS NOT NULL "
                + "AND LONGITUDE IS NOT NULL";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            List<WeightedLatLng> data;
            @Override
            public void onDBCursorReady(Cursor cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        data = new ArrayList<>();
                        do {
                            LatLng latlng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                            float intensity = (cursor.getInt(2) - yearReduction);
                            WeightedLatLng wlatlng = new WeightedLatLng(latlng, intensity);
                            data.add(wlatlng);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(BuildingAgeLayer.class.getSimpleName(), e.getMessage());
                    data = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                callback.onMapLayerDataReady(LAYER_TYPE, data);
            }
        }, sqlQuery).execute();
    }

    /**
     * Get aggregate data from SQL.
     * @param selectStatement of SQL query
     * @return aggregate value as int
     */
    private int getAggregate(String selectStatement) {
        String buildingAgeTableName = DataSetType.BUILDING_AGE.getDataSet().getTableName();
        String sql = "SELECT " + selectStatement + " FROM " + buildingAgeTableName + " "
                + "WHERE BLDGAGE IS NOT NULL "
                + "AND LATITUDE IS NOT NULL "
                + "AND LONGITUDE IS NOT NULL";
        SQLiteDatabase db = DBHelper.GetInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        int aggregate = cursor.getInt(0);
        cursor.close();
        db.close();
        return aggregate;
    }
}

package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.datasets.BuildingAttributesData;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * HighRises MapLayer.
 */
public class HighRisesLayer extends MapLayer {

    // ---------------------------------------------------------------------------------------------
    //                                             STATIC
    // ---------------------------------------------------------------------------------------------

    private final static int          R_STRING_ID_LAYER_NAME;
    private final static int          R_DRAWABLE_ID_ICON;
    private final static MapLayerType LAYER_TYPE;
    private final static int          HEATMAP_RADIUS;
    private final static boolean      HAS_SELECTED_AREA_FUNCTIONS;

    static {
        R_STRING_ID_LAYER_NAME      = R.string.layer_high_rises;
        R_DRAWABLE_ID_ICON          = R.drawable.ic_line_weight_black_24dp;
        LAYER_TYPE                  = MapLayerType.HIGH_RISES;
        HEATMAP_RADIUS              = 30;
        HAS_SELECTED_AREA_FUNCTIONS = false;
    }

    // ---------------------------------------------------------------------------------------------
    //                                          INSTANCE
    // ---------------------------------------------------------------------------------------------

    public HighRisesLayer() {
        super(LAYER_TYPE, R_STRING_ID_LAYER_NAME, R_DRAWABLE_ID_ICON, HEATMAP_RADIUS, HAS_SELECTED_AREA_FUNCTIONS);
    }

    @Override
    public void getMapDataAsync(final OnMapLayerDataReadyCallback callback) {
        float minHeight = getAggregate("MIN(BLDGAGE)");
        String highRisesTableName = DataSetType.HIGH_RISES.getDataSet().getTableName();
        String sqlQuery = "SELECT LATITUDE, LONGITUDE, M_BLDGHGT "
                + "FROM " + highRisesTableName + " "
                + "WHERE M_BLDGHGT IS NOT NULL "
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
                            float intensity = (cursor.getInt(2) - minHeight);
                            WeightedLatLng wlatlng = new WeightedLatLng(latlng, intensity);
                            data.add(wlatlng);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(BuildingAttributesData.class.getSimpleName(), e.getMessage());
                    data = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                callback.onMapLayerDataReady(LAYER_TYPE, data);
            }
        }, sqlQuery).execute();
    }

    private float getAggregate(String selectStatement) {
        String buildingAgeTableName = DataSetType.BUILDING_AGE.getDataSet().getTableName();
        String sql = "SELECT " + selectStatement + " FROM " + buildingAgeTableName + " "
                + "WHERE BLDGAGE IS NOT NULL "
                + "AND LATITUDE IS NOT NULL "
                + "AND LONGITUDE IS NOT NULL";
        SQLiteDatabase db = DBHelper.GetInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        float aggregate = cursor.getFloat(0);
        cursor.close();
        db.close();
        return aggregate;
    }
}

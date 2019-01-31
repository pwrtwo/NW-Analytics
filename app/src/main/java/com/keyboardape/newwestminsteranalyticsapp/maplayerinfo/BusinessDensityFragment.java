package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.gms.maps.model.LatLng;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayer;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * BusinessDensity's MapLayerInfoFragment.
 */
public class BusinessDensityFragment extends MapLayerInfoFragment {

    private TextView             mNoteText;
    private CombinedChart mBusinessAgeChart;
    private PieChart             mBuildingAgePercentChart;

    private int[]                mBuildingAge;
    private Map<String, Integer> mBuildingAgePercentage;

    /**
     * Constructor.
     */
    public BusinessDensityFragment() {
        mNoteText = null;
        mBusinessAgeChart = null;
//        mBuildingAgePercentChart = null;

        mBuildingAge = null;
        mBuildingAgePercentage = null;
    }

    /**
     * Android system calls this function onCreateView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_layer_info_building_age, container, false);

        mNoteText = (TextView) v.findViewById(R.id.textView);
        mBusinessAgeChart = (CombinedChart) v.findViewById(R.id.business_age_chart);
//        mBuildingAgePercentChart = (PieChart) v.findViewById(R.id.building_age_percentage_chart);

        setupBuildingAgeChart();
        setupBuildingAgePercentChart();

        return v;
    }

    /**
     * Android system calls this function onViewCreated.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reloadLayerInfo();
    }

    /**
     * Reloads MapLayerInfoFragment.
     */
    @Override
    public void reloadLayerInfo() {
        if (!isVisible()) {
            return;
        }

        LatLng[] selectedArea = MapLayer.GetSelectedAreaOrNull();
        if (selectedArea != null) {
            loadSelectedAreaFromDB(selectedArea);
        } else {
            loadWholeCityFromDB();
        }
    }

    // ---------------------------------------------------------------------------------------------
    //                                         LOADING DATA
    // ---------------------------------------------------------------------------------------------

    /**
     * Loads data for selected area.
     * @param selectedArea to load
     */
    private void loadSelectedAreaFromDB(LatLng[] selectedArea) {
        String bldAgeTableName = DataSetType.BUILDING_AGE.getDataSet().getTableName();
        String sqlQuery =
                "SELECT BLDGAGE FROM '" + bldAgeTableName + "' " +
                        "WHERE " + selectedArea[0].latitude + " < LATITUDE AND LATITUDE < " + selectedArea[2].latitude + " " +
                        "AND " + selectedArea[0].longitude + " < LONGITUDE AND LONGITUDE < " + selectedArea[2].longitude + " " +
                        "AND BLDGAGE IS NOT NULL";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            @Override
            public void onDBCursorReady(Cursor cursor) {
                if (cursor.moveToFirst()) {
                    BuildingAge bldAge = new BuildingAge();
                    do {
                        bldAge.add(cursor.getInt(0));
                    } while (cursor.moveToNext());
                    mBuildingAge = bldAge.getAbsoluteValues();
                    mBuildingAgePercentage = bldAge.getPercentages();
                } else {
                    mBuildingAge = null;
                    mBuildingAgePercentage = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                loadBuildingAgeChart();
                loadBuildingAgePercentChart();
            }
        }, sqlQuery).execute();
    }

    /**
     * Loads data for whole city.
     */
    private void loadWholeCityFromDB() {
        String bldAgeTableName = DataSetType.BUILDING_AGE.getDataSet().getTableName();
        String sqlQuery =
                "SELECT BLDGAGE FROM '" + bldAgeTableName + "' " +
                        "WHERE LONGITUDE IS NOT NULL " +
                        "AND LATITUDE IS NOT NULL " +
                        "AND BLDGAGE IS NOT NULL";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            @Override
            public void onDBCursorReady(Cursor cursor) {
                if (cursor.moveToFirst()) {
                    BuildingAge bldAge = new BuildingAge();
                    do {
                        bldAge.add(cursor.getInt(0));
                    } while (cursor.moveToNext());
                    mBuildingAge = bldAge.getAbsoluteValues();
                    mBuildingAgePercentage = bldAge.getPercentages();
                } else {
                    mBuildingAge = null;
                    mBuildingAgePercentage = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                loadBuildingAgeChart();
                loadBuildingAgePercentChart();
            }
        }, sqlQuery).execute();
    }

    // ---------------------------------------------------------------------------------------------
    //                                            BAR CHART
    // ---------------------------------------------------------------------------------------------

    /**
     * Load Building Age BarChart.
     */
    private void loadBuildingAgeChart() {
        if (mBuildingAge == null) {
            mBuildingAge = new int[0];
        }

        // Generate bar data
        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();
        for (int i = 0; i < mBuildingAge.length; ++i) {
            entries1.add(new BarEntry(Math.abs(mBuildingAge.length - 1 - i) * 5 + 2.5f, mBuildingAge[i]));
        }
        BarDataSet set1 = new BarDataSet(entries1, "Buildings Built");
        set1.setColor(Color.rgb(67,67,72));
        set1.setValueTextColor(Color.rgb(67,67,72));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        BarData selectedArea = new BarData(set1);
        selectedArea.setBarWidth(4.5f);

        // Add combined data
        CombinedData data = new CombinedData();
        data.setData(selectedArea);

        XAxis xAxis = mBusinessAgeChart.getXAxis();
        xAxis.setGranularity(5f);
        xAxis.setAxisMinimum(data.getXMin() - 2.5f);
        xAxis.setAxisMaximum(data.getXMax() + 2.5f);
        xAxis.setLabelCount(selectedArea.getEntryCount());

        mBusinessAgeChart.setData(data);
        mBusinessAgeChart.invalidate();
        mBusinessAgeChart.animateY(1500);
    }

    /**
     * Setup BuildingAge BarChart.
     */
    private void setupBuildingAgeChart() {
        mBusinessAgeChart.getDescription().setEnabled(false);
        mBusinessAgeChart.setScaleEnabled(false);
        mBusinessAgeChart.setPinchZoom(false);
        mBusinessAgeChart.setBackgroundColor(Color.WHITE);
        mBusinessAgeChart.setDrawGridBackground(false);
        mBusinessAgeChart.setDrawBarShadow(false);
        mBusinessAgeChart.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        mBusinessAgeChart.setDrawOrder(new CombinedChart.DrawOrder[] {
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        Legend l = mBusinessAgeChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = mBusinessAgeChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);

        YAxis leftAxis = mBusinessAgeChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxis = mBusinessAgeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int v = currentYear - 4 - Math.abs(55 - ((int) value));
                return v + "-";
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    //                                          PIE CHART
    // ---------------------------------------------------------------------------------------------

    /**
     * Setup BuildingAge PieChart.
     */
    private void setupBuildingAgePercentChart() {
        mBuildingAgePercentChart.setUsePercentValues(true);
        mBuildingAgePercentChart.getDescription().setEnabled(false);
        mBuildingAgePercentChart.setExtraOffsets(5, 10, 5, 5);
        mBuildingAgePercentChart.setDragDecelerationFrictionCoef(0.95f);
        mBuildingAgePercentChart.setCenterText("BUILDING AGE");
        mBuildingAgePercentChart.setDrawHoleEnabled(true);
        mBuildingAgePercentChart.setHoleColor(Color.WHITE);
        mBuildingAgePercentChart.setTransparentCircleColor(Color.WHITE);
        mBuildingAgePercentChart.setTransparentCircleAlpha(110);
        mBuildingAgePercentChart.setHoleRadius(45f);
        mBuildingAgePercentChart.setTransparentCircleRadius(61f);
        mBuildingAgePercentChart.setDrawCenterText(true);
        mBuildingAgePercentChart.setRotationAngle(0);
        // disable rotation of the chart by touch
        mBuildingAgePercentChart.setRotationEnabled(false);
        mBuildingAgePercentChart.setHighlightPerTapEnabled(false);
    }

    /**
     * Load BuildingAge PieChart.
     */
    private void loadBuildingAgePercentChart() {
        if (mBuildingAgePercentage == null) {
            mBuildingAgePercentage = new HashMap<>();
        }

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (Map.Entry<String, Integer> entry : mBuildingAgePercentage.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        mBuildingAgePercentChart.setData(data);
        // undo all highlights
        mBuildingAgePercentChart.highlightValues(null);
        mBuildingAgePercentChart.invalidate();
        mBuildingAgePercentChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);
        Legend l = this.mBuildingAgePercentChart.getLegend();
        l.setEnabled(false);
        // entry label styling
        this.mBuildingAgePercentChart.setEntryLabelColor(Color.BLACK);
        this.mBuildingAgePercentChart.setEntryLabelTextSize(12f);
    }
}

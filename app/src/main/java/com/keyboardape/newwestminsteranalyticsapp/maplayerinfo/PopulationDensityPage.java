package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class PopulationDensityPage extends Fragment {

    public static final String YEAR = "year";

    private PopulationDensity  mPopulationDensity;

    private HorizontalBarChart mAgeDemographicsChart;
    private PieChart           mAgeGroupsChart;
    private int                mYear;

    /**
     * Android system calls onCreateView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_layer_info_population_density_page, container, false);

        mYear = getArguments().getInt(YEAR);
        mPopulationDensity = new PopulationDensity();

        mAgeDemographicsChart = (HorizontalBarChart) v.findViewById(R.id.chart_age_demographics);
        mAgeGroupsChart = (PieChart) v.findViewById(R.id.chart_age_groups);

        setupAgeDemographicsChart();
        setupAgeGroupsChart();

        loadAgeDemographicsFromDB(mYear);

        return v;
    }

    /**
     * Load Age Demographics from database.
     * @param year to load
     */
    private void loadAgeDemographicsFromDB(int year) {
        String sqlQuery = "SELECT YEAR, MINAGE, MALE_POPULATION, FEMALE_POPULATION " +
                "FROM '" + DataSetType.AGE_DEMOGRAPHICS.getDataSet().getTableName() + "' " +
                "WHERE YEAR = " + year + " " +
                "ORDER BY MINAGE DESC";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            @Override
            public void onDBCursorReady(Cursor cursor) {
                if (cursor.moveToFirst()) {
                    do {
                        mPopulationDensity.addPopulation(
                                cursor.getInt(0),
                                cursor.getInt(1),
                                cursor.getInt(2),
                                cursor.getInt(3)
                        );
                    } while (cursor.moveToNext());
                }
            }
            @Override
            public void onDBReadComplete() {
                loadAgeDemographicsChart();
                loadAgeGroupsChart();
            }
        }, sqlQuery).execute();
    }

    // ---------------------------------------------------------------------------------------------
    //                                  AGE DEMOGRAPHICS BAR CHART
    // ---------------------------------------------------------------------------------------------

    /**
     * Setup age demographics chart.
     */
    private void setupAgeDemographicsChart() {
        mAgeDemographicsChart.setDrawGridBackground(false);
        mAgeDemographicsChart.getDescription().setEnabled(false);
        mAgeDemographicsChart.setScaleEnabled(false);
        mAgeDemographicsChart.setPinchZoom(false);
        mAgeDemographicsChart.setDrawBarShadow(false);
        mAgeDemographicsChart.setDrawValueAboveBar(true);
        mAgeDemographicsChart.setHighlightFullBarEnabled(false);
        mAgeDemographicsChart.getAxisLeft().setEnabled(false);
        mAgeDemographicsChart.getAxisRight().setDrawGridLines(false);
        mAgeDemographicsChart.getAxisRight().setDrawZeroLine(true);
        mAgeDemographicsChart.getAxisRight().setLabelCount(7, false);
        mAgeDemographicsChart.getAxisRight().setValueFormatter(new CustomFormatter());
        mAgeDemographicsChart.getAxisRight().setTextSize(9f);
        XAxis xAxis = mAgeDemographicsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextSize(9f);
        xAxis.setAxisMinimum(0f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(5f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private DecimalFormat format = new DecimalFormat("###");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                // 2016 demographics data goes up to 100
                // other years goes up to 80
                if ((mYear == 2016 && (int)value == 100)
                        || (mYear != 2016 && (int)value == 85)) {
                    return format.format(value) + "+";
                }
                return format.format(value) + "-" + format.format(value + 5);
            }
        });
        Legend l = mAgeDemographicsChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);
    }

    /**
     * Load age demographics chart.
     */
    private void loadAgeDemographicsChart() {
        // IMPORTANT: When using negative values in stacked bars, always make sure the negative values are in the array first
        ArrayList<BarEntry> yValues = new ArrayList<BarEntry>();
        for (Map.Entry<Integer, Pair<Integer, Integer>> entry : mPopulationDensity.getDemographics(mYear).entrySet()) {
            Integer ageGroup = entry.getKey();
            Pair<Integer, Integer> population = entry.getValue();
            yValues.add(new BarEntry(ageGroup + 2.5f, new float[]{ population.first * -1, population.second }));
        }
        BarDataSet set = new BarDataSet(yValues, "");
        set.setDrawValues(true);
        set.setDrawIcons(false);
        set.setValueFormatter(new CustomFormatter());
        set.setValueTextSize(7f);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setColors(new int[] {Color.rgb(67,67,72), Color.rgb(124,181,236)});
        set.setStackLabels(new String[]{
                "Men " + mPopulationDensity.getTotalMalePercent(mYear) + "%",
                "Women " + mPopulationDensity.getTotalFemalePercent(mYear) + "%"
        });
        BarData data = new BarData(set);
        data.setBarWidth(4.5f);

        XAxis xAxis = mAgeDemographicsChart.getXAxis();
        float xMax = set.getXMax() + 2.5f;
        int labelCount = (int) xMax / 5;
        xAxis.setAxisMaximum(xMax);
        xAxis.setLabelCount(labelCount);

        mAgeDemographicsChart.setData(data);
        mAgeDemographicsChart.invalidate();
        mAgeDemographicsChart.animateY(1000);
    }

    // ---------------------------------------------------------------------------------------------
    //                                      AGE GROUPS PIE CHART
    // ---------------------------------------------------------------------------------------------

    /**
     * Setup age groups chart.
     */
    private void setupAgeGroupsChart() {
        mAgeGroupsChart.setUsePercentValues(true);
        mAgeGroupsChart.getDescription().setEnabled(false);
        mAgeGroupsChart.setExtraOffsets(5, 10, 5, 5);
        mAgeGroupsChart.setDragDecelerationFrictionCoef(0.95f);
//        mAgeGroupsChart.setCenterTextTypeface(mTfLight);
        mAgeGroupsChart.setCenterText("AGE GROUPS");
        mAgeGroupsChart.setDrawHoleEnabled(true);
        mAgeGroupsChart.setHoleColor(Color.WHITE);
        mAgeGroupsChart.setTransparentCircleColor(Color.WHITE);
        mAgeGroupsChart.setTransparentCircleAlpha(110);
        mAgeGroupsChart.setHoleRadius(45f);
        mAgeGroupsChart.setTransparentCircleRadius(61f);
        mAgeGroupsChart.setDrawCenterText(true);
        mAgeGroupsChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mAgeGroupsChart.setRotationEnabled(false);
        mAgeGroupsChart.setHighlightPerTapEnabled(false);
    }

    /**
     * Load age groups chart.
     */
    private void loadAgeGroupsChart() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (Map.Entry<Integer, Long> entry : mPopulationDensity.getAgeGroups(mYear).entrySet()) {
            String label;
            if (entry.getKey() == 0) {
                label = "Children (0-15)"; // Children
            } else if (entry.getKey() == 1) {
                label = "Youth (15-25)"; // Youth
            } else if (entry.getKey() == 2) {
                label = "Adults (25-65)"; // Adults
            } else {
                label = "Seniors (65+)"; // Seniors
            }
            entries.add(new PieEntry(entry.getValue(), label));
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
        mAgeGroupsChart.setData(data);
        // undo all highlights
        mAgeGroupsChart.highlightValues(null);
        mAgeGroupsChart.invalidate();
        mAgeGroupsChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);
        Legend l = this.mAgeGroupsChart.getLegend();
        l.setEnabled(false);
        // entry label styling
        this.mAgeGroupsChart.setEntryLabelColor(Color.BLACK);
        this.mAgeGroupsChart.setEntryLabelTextSize(12f);
    }

    // ---------------------------------------------------------------------------------------------
    //                                      CUSTOM FORMATTERS
    // ---------------------------------------------------------------------------------------------

    /**
     * Custom formatter.
     */
    private class CustomFormatter implements IValueFormatter, IAxisValueFormatter
    {
        private DecimalFormat mFormat;
        public CustomFormatter() {
            mFormat = new DecimalFormat("###");
        }
        // data
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(Math.abs(value)) + "";
        }
        // YAxis
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mFormat.format(Math.abs(value)) + "";
        }
    }
}
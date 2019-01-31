// TODO: 9/29/2017 find out what barChart.invalidate(); does

package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.keyboardape.newwestminsteranalyticsapp.DBActivity;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.BusinessLicensesData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChartActivity extends DBActivity {


    BarChart barChart;
    float barWidth;
    private SQLiteDatabase db;
    private Map<String,Float> graphCount = new HashMap<String,Float>();

    //Sorted list
    private List<Map.Entry<String, Float>> list;

    private String a[];
    private String myString = "Sending String from ChartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Setup toolbar and title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_charts));
        readDb();
    }

    private void readDb()
    {
        // Get the SQL Statement
        String chartTableName = DataSetType.BUSINESS_LICENSES.getDataSet().getTableName();
        String sqlQuery = "SELECT SIC_GROUP " + "FROM " + chartTableName;

        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            @Override
            public void onDBCursorReady(Cursor cursor) {
                int count = cursor.getCount();
                a = new String[count];
                try {
                    if (cursor.moveToFirst()) {
                        int ndx = 0;
                        do {
                            a[ndx] = cursor.getString(0);
                            //Ugly hash code to count most popular businesses
                            Float freq = graphCount.get(a[ndx]);
                            graphCount.put(a[ndx], (freq == null) ? 1 : freq +1);
                            ndx++;
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(BusinessLicensesData.class.getSimpleName(),e.getMessage());
                    a = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                printChart();
                printTop10();
            }
        }, sqlQuery).execute();
    }

    private void printChart()
    {
        // Set up the labels
        String[] labels = new String[11];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Integer.toString(i);
        }

        // Declarations
        barChart = (BarChart) findViewById(R.id.bargraph);
        barWidth = 0.9f;

        // Set animation to make it cool
        barChart.animateY(3000);

        // X-Axis settings
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setLabelCount(10);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Y-Axis settings
        barChart.getAxisLeft().setAxisMinimum(0); // Starts at 0
        barChart.getAxisRight().setAxisMinimum(0); // Starts at 0

        //Make the description empty
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        // To store the data in somehow
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        // Really ugly sort code to sort the hashmap to values
        Set<Map.Entry<String, Float>> set = graphCount.entrySet();
        list = new ArrayList<Map.Entry<String, Float>>(set);
        Collections.sort( list, new Comparator<Map.Entry<String, Float>>()
            {
                public int compare( Map.Entry<String, Float> o1, Map.Entry<String, Float> o2 )
                {
                    return (o2.getValue()).compareTo( o1.getValue() );
                }
            }
        );

        //Help gets the top 10 most popular businesses
        int i = 1;
        int topTen = 0;
        for (Map.Entry<String, Float> entry:list) {
            if (entry.getValue() > 1 && topTen < 10) {
                barEntries.add(new BarEntry(i++, entry.getValue())); //Entries must be floats
                barChart.notifyDataSetChanged();
                barChart.invalidate();
                System.out.println(i + " " + entry.getValue());
                topTen++;
            }
        }

        // Is needed so the data can be outputted
        BarDataSet barDataSet = new BarDataSet(barEntries,"Most Popular Businesses");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(barDataSet);

        barChart.setData(data);

        // Settings, Needs to be declared at the very bottom
        data.setBarWidth(barWidth);
        barChart.getData().setHighlightEnabled(false); // Prevents tapping on the bars
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setFitBars(true); // makes the x-axis fit exactly all bars
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.invalidate(); // refreshes (redraws the chart)

        testLegend(barChart);
    }

    //Makes the list of the top 10 most popular businesses
    private void printTop10() {
        String[] items = new String[10];
        int i = 0;
        for (Map.Entry<String, Float> entry:list) {
            if (i < 10) {
                items[i] = (i+1) + ". "+ entry.getKey();
                i++;
            }
        }
        ListView list_topten = (ListView) findViewById(R.id.topTen);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_expandable_list_item_1, items
        );
        list_topten.setAdapter(arrayAdapter);
    }

    private void testLegend(BarChart barChart) {
        Legend l = barChart.getLegend();
        l.setEnabled(false);
    }

    public String getMyData() {
        return myString;
    }
}

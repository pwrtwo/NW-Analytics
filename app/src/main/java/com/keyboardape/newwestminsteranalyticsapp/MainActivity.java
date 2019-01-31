package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetAdapter;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;

/**
 * Application entry point.
 */
public class MainActivity extends DBActivity implements DataSet.OnDataSetUpdatedCallback {

    private DataSet[] mDataSets;
    private int       mCurrentDataSet;

    private ListView mDownloadList;
    private DataSetAdapter mDataSetAdapter;
    private Button   mBtnViewMaps;
    private Button   mBtnViewCharts;
//    private Button   mBtnViewDemographics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataSet.Initialize(this);

        mDataSets = DataSet.GetAllDataSets();
        mCurrentDataSet = 0;

        mDataSetAdapter = new DataSetAdapter(this, mDataSets);
        mDownloadList = (ListView) findViewById(R.id.downloadList);
        mDownloadList.setAdapter(mDataSetAdapter);

        mBtnViewMaps = (Button) findViewById(R.id.btnViewMaps);
        mBtnViewMaps.setEnabled(false);
        mBtnViewCharts = (Button) findViewById(R.id.btnViewCharts);
        mBtnViewCharts.setEnabled(false);
//        mBtnViewDemographics = (Button) findViewById(R.id.btnViewDemographics);
//        mBtnViewDemographics.setEnabled(false);

        downloadDataSetOrEnableButtons();
    }

    public void onClickViewMaps(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void onClickViewCharts(View v) {
        Intent i = new Intent(this, ChartActivity.class);
        startActivity(i);
    }

//    public void onClickViewDemographics(View v) {
//        Intent i = new Intent(this, Demo2001Activity.class);
//        startActivity(i);
//    }

    private void downloadDataSetOrEnableButtons() {
        mDataSetAdapter.notifyDataSetChanged();
        if (mCurrentDataSet++ < mDataSets.length) {
            DataSet data = mDataSets[mCurrentDataSet - 1];
            data.isRequireUpdateAsync(new DataSet.OnDataSetRequireUpdate() {
                @Override
                public void onDataSetRequireUpdate(boolean isRequireUpdate) {
                    if (isRequireUpdate) {
                        data.updateDataAsync(MainActivity.this);
                    } else {
                        mDataSetAdapter.notifyDataSetChanged();
                        downloadDataSetOrEnableButtons();
                    }
                }
            });
        } else {
            mBtnViewMaps.setEnabled(true);
            mBtnViewCharts.setEnabled(true);
//            mBtnViewDemographics.setEnabled(true);
        }
    }

    @Override
    public void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful) {
        if (!isUpdateSuccessful) {
            Toast.makeText(this, "Download data failed...", Toast.LENGTH_LONG).show();
        }
        mDataSetAdapter.notifyDataSetChanged();
        downloadDataSetOrEnableButtons();
    }
}

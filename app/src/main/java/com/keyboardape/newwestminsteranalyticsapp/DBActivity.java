package com.keyboardape.newwestminsteranalyticsapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;

/**
 * Base Database Activity.
 *
 * Ensures children will always have a valid DBHelper object.
 */
public abstract class DBActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBHelper.Initialize(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DBHelper.Initialize(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DBHelper.SetActivityStopped();
    }
}
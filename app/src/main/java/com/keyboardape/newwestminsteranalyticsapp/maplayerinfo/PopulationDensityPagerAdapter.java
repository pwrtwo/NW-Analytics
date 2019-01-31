package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.keyboardape.newwestminsteranalyticsapp.datasets.AgeDemographicsData;

public class PopulationDensityPagerAdapter extends FragmentPagerAdapter {

    public PopulationDensityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        int[] years = AgeDemographicsData.CENSUS_DATA_YEARS;
        Fragment fragment = new PopulationDensityPage();
        Bundle args = new Bundle();
        args.putInt(PopulationDensityPage.YEAR, years[i]);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return AgeDemographicsData.CENSUS_DATA_YEARS.length;
    }
}
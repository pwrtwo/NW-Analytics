package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.AgeDemographicsData;

/**
 * PopulationDensity's MapLayerInfoFragment.
 */
public class PopulationDensityFragment extends MapLayerInfoFragment implements ViewPager.OnPageChangeListener {

    private Button[]  mYearButtons;
    private ViewPager mViewPager;

    /**
     * Constructor.
     */
    public PopulationDensityFragment() {
    }

    /**
     * Called by Android's system onCreateView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_layer_info_population_dentisy, container, false);

        // set up page viewer
        mViewPager = (ViewPager) v.findViewById(R.id.pager);
        mViewPager.setAdapter(new PopulationDensityPagerAdapter(getChildFragmentManager()));
        mViewPager.addOnPageChangeListener(this);

        // load buttons and set up event listeners
        addCensusYearButtons((LinearLayout) v.findViewById(R.id.census_years));
        onPageSelected(0);

        return v;
    }

    /**
     * Reloads MapLayerInfoFragment.
     */
    @Override
    public void reloadLayerInfo() {
    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < AgeDemographicsData.CENSUS_DATA_YEARS.length; ++i) {
            if (i == position) {
                mYearButtons[i].setEnabled(false);
            } else {
                mYearButtons[i].setEnabled(true);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    private void addCensusYearButtons(LinearLayout v) {
        mYearButtons = new Button[AgeDemographicsData.CENSUS_DATA_YEARS.length];
        for (int i = 0; i < AgeDemographicsData.CENSUS_DATA_YEARS.length; ++i) {
            Button newButton = new Button(getContext());
            newButton.setText(AgeDemographicsData.CENSUS_DATA_YEARS[i] + "");
            setupOnClick(newButton, i);
            v.addView(newButton);
            mYearButtons[i] = newButton;
        }
    }

    private void setupOnClick(Button button, final int index) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(index, true);
            }
        });
    }
}

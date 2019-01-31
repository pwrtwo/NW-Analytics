package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Building Age Data Object.
 */
public class BuildingAge {

    public static final int YEARS_TO_TRACK = 60;
    public static final int NUMBER_OF_YEARS_TO_GROUP_BY = 5;

    private int   mCurrentYear;
    private int[] mBuildingsAge;
    private long  mTotalBuildings;

    /**
     * Constructor.
     */
    public BuildingAge() {
        mCurrentYear = Calendar.getInstance().get(Calendar.YEAR);
        mBuildingsAge = new int[YEARS_TO_TRACK / NUMBER_OF_YEARS_TO_GROUP_BY];
    }

    /**
     * Add a building age entry.
     * @param yearBuilt entry
     */
    public void add(int yearBuilt) {
        int yearsOld = mCurrentYear - yearBuilt;
        int index = yearsOld / NUMBER_OF_YEARS_TO_GROUP_BY;

        if (index < mBuildingsAge.length) {
            ++mBuildingsAge[index];
        }

        ++mTotalBuildings;
    }

    /**
     * Return count of buildings age groupd by NUMBER_OF_YEARS_TO_GROUP_BY.
     * @return int[]
     */
    public int[] getAbsoluteValues() {
        return mBuildingsAge;
    }

    /**
     * Return building age percentages grouped.
     * @return Map
     */
    public Map<String, Integer> getPercentages() {
        Map<String, Integer> sum = new LinkedHashMap<>();
        long untracked = mTotalBuildings;
        for (int i = 0; i < mBuildingsAge.length; ++i) {
            untracked -= mBuildingsAge[i];
            int minYear = i * 5;
            int maxYear = minYear + NUMBER_OF_YEARS_TO_GROUP_BY;
            if (maxYear <= 10) {
                String key = "0-10 yrs";
                Integer buildingsBuilt = sum.get(key);
                buildingsBuilt = (buildingsBuilt == null)
                        ? mBuildingsAge[i]
                        : mBuildingsAge[i] + buildingsBuilt;
                sum.put(key, buildingsBuilt);
            } else if (maxYear <= 30) {
                String key = "10-30 yrs";
                Integer buildingsBuilt = sum.get(key);
                buildingsBuilt = (buildingsBuilt == null)
                        ? mBuildingsAge[i]
                        : mBuildingsAge[i] + buildingsBuilt;
                sum.put(key, buildingsBuilt);
            } else if (maxYear <= 60) {
                String key = "30-60 yrs";
                Integer buildingsBuilt = sum.get(key);
                buildingsBuilt = (buildingsBuilt == null)
                        ? mBuildingsAge[i]
                        : mBuildingsAge[i] + buildingsBuilt;
                sum.put(key, buildingsBuilt);
            }
        }
        sum.put("60+ yrs", (int) untracked);

        Map<String, Integer> percentage = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : sum.entrySet()) {
            int percent = (int)(entry.getValue() * 100 / mTotalBuildings);
            if (percent > 0) {
                percentage.put(entry.getKey(), percent);
            }
        }
        return percentage;
    }
}
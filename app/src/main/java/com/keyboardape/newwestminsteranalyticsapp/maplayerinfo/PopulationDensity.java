package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.util.Pair;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PopulationDensity data object.
 */
public class PopulationDensity {

    // Map<Year, Map<AgeGroupNum, AgeGroupPopulation>>
    // AgeGroupNum: 0 =  0 - 14 yrs old
    //              1 = 15 - 24 yrs old
    //              2 = 25 - 64 yrs old
    //              3 = 65+ yrs old
    private Map<Integer, Map<Integer, Long>>                   mAgeGroups;

    // Map<Year, Map<AgeFrom, Pair<MalePopulation, FemalePopulation>>
    private Map<Integer, Map<Integer, Pair<Integer, Integer>>> mPopulation;

    // Map<Year, MalePopulation>
    private Map<Integer, Long>                                 mTotalMale;

    // Map<Year, FemalePopulation>
    private Map<Integer, Long>                                 mTotalFemale;

    public PopulationDensity() {
        mAgeGroups   = new LinkedHashMap<>();
        mPopulation  = new LinkedHashMap<>();
        mTotalMale   = new HashMap<>();
        mTotalFemale = new HashMap<>();
    }

    public int getTotalMalePercent(int year) {
        return (int) (getTotalMale(year) * 100 / (getTotalFemale(year) + getTotalMale(year)));
    }

    public int getTotalFemalePercent(int year) {
        return (int) (getTotalFemale(year) * 100 / (getTotalMale(year) + getTotalFemale(year)));
    }

    public Long getTotalMale(int year) {
        return mTotalMale.get(year);
    }

    public Long getTotalFemale(int year) {
        return mTotalFemale.get(year);
    }

    public Map<Integer, Pair<Integer, Integer>> getDemographics(int year) {
        return mPopulation.get(year);
    }

    public Map<Integer, Long> getAgeGroups(int year) {
        return mAgeGroups.get(year);
    }

    public void addPopulation(int year, int ageFrom, int malePopulation, int femalePopulation) {

        // Update total male count
        Long totalMale = mTotalMale.get(year);
        mTotalMale.put(year, (totalMale != null) ? malePopulation + totalMale : malePopulation);

        // Update total female count
        Long totalFemale = mTotalFemale.get(year);
        mTotalFemale.put(year, (totalFemale != null) ? femalePopulation + totalFemale : femalePopulation);

        // Update age groups count
        Map<Integer, Long> ageGroups = getAgeGroups(year);
        if (ageGroups == null) {
            ageGroups = new LinkedHashMap<>();
            mAgeGroups.put(year, ageGroups);
        }
        int ageGroupNum;
        if (ageFrom < 15) {
            ageGroupNum = 0; // Children
        } else if (ageFrom < 25) {
            ageGroupNum = 1; // Youth
        } else if (ageFrom < 65) {
            ageGroupNum = 2; // Adults
        } else {
            ageGroupNum = 3; // Seniors
        }
        Long ageGroupCount = ageGroups.get(ageGroupNum);
        ageGroups.put(ageGroupNum, (ageGroupCount != null)
                ? malePopulation + femalePopulation + ageGroupCount
                : malePopulation + femalePopulation);

        // New map if demographics for year not created
        Map<Integer, Pair<Integer, Integer>> demographics = getDemographics(year);
        if (demographics == null) {
            demographics = new LinkedHashMap<>();
            mPopulation.put(year, demographics);
        }
        demographics.put(ageFrom, new Pair<Integer, Integer>(malePopulation, femalePopulation));
    }
}
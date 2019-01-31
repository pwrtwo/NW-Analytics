package com.keyboardape.newwestminsteranalyticsapp.datasets;

/**
 * Valid DataSet types.
 */
public enum DataSetType {

    BUILDING_ATTRIBUTES
    ,SKYTRAIN_STATIONS
    ,BUS_STOPS
    ,BUSINESS_LICENSES
    ,MAJOR_SHOPPING
    ,BUILDING_AGE
    ,HIGH_RISES
    ,AGE_DEMOGRAPHICS
    ,SCHOOL_BUILDINGS
    ;

    public DataSet getDataSet() {
        return DataSet.GetDataSet(this);
    }

}

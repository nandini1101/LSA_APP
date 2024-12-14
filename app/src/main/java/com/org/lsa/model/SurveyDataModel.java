package com.org.lsa.model;
public class SurveyDataModel {
    private int zoneId;
    private int wardNo;
    private int main_SDId;
    private String establishmentType;
    private String houseNo;
    private String ownerName;
    private String ownerContactNo;
    private String address;
    private int default_months;
    private int total_Outstadnding_amount;

    public int getSDId() {
        return main_SDId;
    }

    public int getWardno() {
        return wardNo;
    }

    public int getTotal_Outstadnding_amount() {
        return total_Outstadnding_amount;
    }

    public int getDefault_months() {
        return default_months;
    }

    public String getZoneId() {
        return zoneId+"";
    }

    public String getHouseNo() {
        return houseNo;
    }

    public String getEstablishmentType() {
        return establishmentType;
    }

    public String getOwnerContactNo() {
        return ownerContactNo;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getAddress() {
        return address;
    }
}
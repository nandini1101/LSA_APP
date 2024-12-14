package com.org.lsa.model;
public class DayEndCollection {
    private int zoneId;
    private int wardNo;
    private String repDate;
    private String collectedBy;
    private String ownerShipType;
    private String houseNo;
    private String ownerName;
    private String ownerContactNo;
    private int months_covered;
    private float amount_paid;
    private String payment_referenceNo;
    private String payment_details;
    private String chq_dd_ne_rt_oth_no;



    public String getZoneId() {
        return zoneId+"";
    }

    public int getWardno() {
        return wardNo;
    }

    public String getdate(){
        return repDate;
    }

    public String getCollectedBy(){
        return collectedBy;
    }



    public String getHouseNo() {
        return houseNo;
    }

    public String getOwnerContactNo() {
        return ownerContactNo;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getMonths_covered(){
        return months_covered;
    }
    public float getAmount_paid(){
        return amount_paid;
    }

    public String getPayment_referenceNo(){ return payment_referenceNo;}
    public String getPayment_details(){return payment_details;}
    public String getChq_dd_ne_rt_oth_no(){return chq_dd_ne_rt_oth_no;}
}
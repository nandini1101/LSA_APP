package com.org.lsa.model;

public class DataModal {

    private String userId;
    private String surveyDetailId;
    private String surveySubDetailId;
    private String phoneNumber;

    private String StatusCode;
    private String StatusMessage;

    public int sdId;
    public int zoneId;
    public String zoneNo;
    public String wardNo;
    public int establishmentTypeId;
    public int ownerShipTypeId;
    public String houseNo;
    public String tagId;
    public int amount_collected;
    public String collected_date;
    public String collected_period;
    public String receipt_number;
    public String receipt_img;
    public boolean sync_server;
    public String sync_date;

    public DataModal(String statusCode, String statusMessage) {
        StatusCode = statusCode;
        StatusMessage = statusMessage;
    }

    public DataModal(String userId, String surveyDetailId, String surveySubDetailId, String phoneNumber) {
        this.userId = userId;
        this.surveyDetailId = surveyDetailId;
        this.surveySubDetailId = surveySubDetailId;
        this.phoneNumber = phoneNumber;
    }

    public DataModal(int sdId, int zoneId, String zoneNo, String wardNo, int establishmentTypeId, int ownerShipTypeId, String houseNo,
                     String tagId, int amount_collected, String collected_date, String collected_period, String receipt_number,
                     String receipt_img, boolean sync_server, String sync_date) {
        this.sdId = sdId;
        this.zoneId = zoneId;
        this.zoneNo = zoneNo;
        this.wardNo = wardNo;
        this.establishmentTypeId = establishmentTypeId;
        this.ownerShipTypeId = ownerShipTypeId;
        this.houseNo = houseNo;
        this.tagId = tagId;
        this.amount_collected = amount_collected;
        this.collected_date = collected_date;
        this.collected_period = collected_period;
        this.receipt_number = receipt_number;
        this.receipt_img = receipt_img;
        this.sync_server = sync_server;
        this.sync_date = sync_date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSurveyDetailId() {
        return surveyDetailId;
    }

    public void setSurveyDetailId(String surveyDetailId) {
        this.surveyDetailId = surveyDetailId;
    }

    public String getSurveySubDetailId() {
        return surveySubDetailId;
    }

    public void setSurveySubDetailId(String surveySubDetailId) {
        this.surveySubDetailId = surveySubDetailId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatusCode() {
        return StatusCode;
    }

    public String getStatusMessage() {
        return StatusMessage;
    }

    public int getSdId() {
        return sdId;
    }

    public void setSdId(int sdId) {
        this.sdId = sdId;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneNo() {
        return zoneNo;
    }

    public void setZoneNo(String zoneNo) {
        this.zoneNo = zoneNo;
    }

    public String getWardNo() {
        return wardNo;
    }

    public void setWardNo(String wardNo) {
        this.wardNo = wardNo;
    }

    public int getEstablishmentTypeId() {
        return establishmentTypeId;
    }

    public void setEstablishmentTypeId(int establishmentTypeId) {
        this.establishmentTypeId = establishmentTypeId;
    }

    public int getOwnerShipTypeId() {
        return ownerShipTypeId;
    }

    public void setOwnerShipTypeId(int ownerShipTypeId) {
        this.ownerShipTypeId = ownerShipTypeId;
    }

    public String getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(String houseNo) {
        this.houseNo = houseNo;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public int getAmount_collected() {
        return amount_collected;
    }

    public void setAmount_collected(int amount_collected) {
        this.amount_collected = amount_collected;
    }

    public String getCollected_date() {
        return collected_date;
    }

    public void setCollected_date(String collected_date) {
        this.collected_date = collected_date;
    }

    public String getCollected_period() {
        return collected_period;
    }

    public void setCollected_period(String collected_period) {
        this.collected_period = collected_period;
    }

    public String getReceipt_number() {
        return receipt_number;
    }

    public void setReceipt_number(String receipt_number) {
        this.receipt_number = receipt_number;
    }

    public String getReceipt_img() {
        return receipt_img;
    }

    public void setReceipt_img(String receipt_img) {
        this.receipt_img = receipt_img;
    }

    public boolean isSync_server() {
        return sync_server;
    }

    public void setSync_server(boolean sync_server) {
        this.sync_server = sync_server;
    }

    public String getSync_date() {
        return sync_date;
    }

    public void setSync_date(String sync_date) {
        this.sync_date = sync_date;
    }
}

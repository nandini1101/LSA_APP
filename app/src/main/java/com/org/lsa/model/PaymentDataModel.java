package com.org.lsa.model;

public class PaymentDataModel {

    private String SDId, OwnerContactNo, Lat, Lng, Img1Url, UserId, UpdatedDate, UpdatedBy,
            Payment_Months, Paid_Amount, Transaction_Pic, Transaction_Ref_No, Temp1,
            Temp2, Temp3, Temp4, Temp5;

    public PaymentDataModel() {
    }

    public PaymentDataModel(String SDId, String ownerContactNo, String lat, String lng, String img1Url, String userId,
                            String updatedDate, String updatedBy, String payment_Months, String paid_Amount, String transaction_Pic,
                            String transaction_Ref_No, String temp1, String temp2, String temp3, String temp4, String temp5) {
        this.SDId = SDId;
        OwnerContactNo = ownerContactNo;
        Lat = lat;
        Lng = lng;
        Img1Url = img1Url;
        UserId = userId;
        UpdatedDate = updatedDate;
        UpdatedBy = updatedBy;
        Payment_Months = payment_Months;
        Paid_Amount = paid_Amount;
        Transaction_Pic = transaction_Pic;
        Transaction_Ref_No = transaction_Ref_No;
        Temp1 = temp1;
        Temp2 = temp2;
        Temp3 = temp3;
        Temp4 = temp4;
        Temp5 = temp5;
    }

    public String getSDId() {
        return SDId;
    }

    public void setSDId(String SDId) {
        this.SDId = SDId;
    }

    public String getOwnerContactNo() {
        return OwnerContactNo;
    }

    public void setOwnerContactNo(String ownerContactNo) {
        OwnerContactNo = ownerContactNo;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLng() {
        return Lng;
    }

    public void setLng(String lng) {
        Lng = lng;
    }

    public String getImg1Url() {
        return Img1Url;
    }

    public void setImg1Url(String img1Url) {
        Img1Url = img1Url;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUpdatedDate() {
        return UpdatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        UpdatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return UpdatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        UpdatedBy = updatedBy;
    }

    public String getPayment_Months() {
        return Payment_Months;
    }

    public void setPayment_Months(String payment_Months) {
        Payment_Months = payment_Months;
    }

    public String getPaid_Amount() {
        return Paid_Amount;
    }

    public void setPaid_Amount(String paid_Amount) {
        Paid_Amount = paid_Amount;
    }

    public String getTransaction_Pic() {
        return Transaction_Pic;
    }

    public void setTransaction_Pic(String transaction_Pic) {
        Transaction_Pic = transaction_Pic;
    }

    public String getTransaction_Ref_No() {
        return Transaction_Ref_No;
    }

    public void setTransaction_Ref_No(String transaction_Ref_No) {
        Transaction_Ref_No = transaction_Ref_No;
    }

    public String getTemp1() {
        return Temp1;
    }

    public void setTemp1(String temp1) {
        Temp1 = temp1;
    }

    public String getTemp2() {
        return Temp2;
    }

    public void setTemp2(String temp2) {
        Temp2 = temp2;
    }

    public String getTemp3() {
        return Temp3;
    }

    public void setTemp3(String temp3) {
        Temp3 = temp3;
    }

    public String getTemp4() {
        return Temp4;
    }

    public void setTemp4(String temp4) {
        Temp4 = temp4;
    }

    public String getTemp5() {
        return Temp5;
    }

    public void setTemp5(String temp5) {
        Temp5 = temp5;
    }
}

package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/07/30.
 * set Json data by Gson.
 */

import com.google.gson.annotations.SerializedName;

public class JsonToiletInfoModel {
    @SerializedName("toiletName")
    private String toiletName;

    @SerializedName("district")
    private String district;

    @SerializedName("municipality")
    private String municipality;

    @SerializedName("address")
    private String address;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("availableTime")
    private String availableTime;

    @SerializedName("hasMultiPurposeToilet")
    private boolean hasMultiPurposeToilet;

    public void SetToiletName(String newValue){
        toiletName = newValue;
    }
    public String GetToiletName(){
        return toiletName;
    }

    public void SetDistrict(String newValue){
        district = newValue;
    }
    public String GetDistrict(){
        return district;
    }

    public void SetMunicipality(String newValue){
        municipality = newValue;
    }
    public String GetMunicipality(){
        return municipality;
    }

    public void SetAddress(String newValue){
        address = newValue;
    }
    public String GetAddress(){
        return address;
    }

    public void SetLatitude(double newValue){
        latitude = newValue;
    }
    public double GetLatitude(){
        return latitude;
    }

    public void SetLongitude(double newValue){
        longitude = newValue;
    }
    public double GetLongitude(){
        return longitude;
    }

    public void SetAvailableTime(String newValue){
        availableTime = newValue;
    }
    public String GetAvailableTime(){
        return availableTime;
    }

    public void SetHasMultiPurposeToilet(boolean newValue){
        hasMultiPurposeToilet = newValue;
    }
    public boolean GetHasMultiPurposeToilet(){
        return hasMultiPurposeToilet;
    }
}

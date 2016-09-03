package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/07/30.
 * set Json data by Gson.
 */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ToiletInfoClass {
    @SerializedName("toiletInfo")
    private ArrayList<ToiletInfo> toiletInfoList;

    public ArrayList<ToiletInfo> getToiletInfoList(){
        return toiletInfoList;
    }
    public void setToiletInfoList(ArrayList<ToiletInfo> newValue){
        toiletInfoList = newValue;
    }
    public int getInfoCount(){
        if(toiletInfoList == null){
            return 0;
        }
        return toiletInfoList.size();
    }

    public class ToiletInfo{
        @SerializedName("toiletName")
        public String toiletName;

        @SerializedName("district")
        public String district;

        @SerializedName("municipality")
        public String municipality;

        @SerializedName("address")
        public String address;

        @SerializedName("latitude")
        public double latitude;

        @SerializedName("longitude")
        public double longitude;

        @SerializedName("availableTime")
        public String availableTime;

        @SerializedName("hasMultiPurposeToilet")
        public boolean hasMultiPurposeToilet;


    }


}

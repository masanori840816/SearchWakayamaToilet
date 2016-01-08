package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2015/12/31.
 */

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseAccesser extends SQLiteOpenHelper{
    public class ToiletInfoModel{
        public int id = 0;
        public String toiletName;
        public String district;
        public String municipality;
        public String address;
        public double latitude;
        public double longitude;
        public String availableTime;
        public String nearbySightseeing;
    }
    public DatabaseAccesser(Context context){
        super(context, "toiletinfo.db", null, 1);
    }
    public void insertInfo(SQLiteDatabase db, ToiletInfoModel toiletInfo){
        // beginTransaction, endTransactionは呼び出し元で実行.
        ContentValues contentValues = new ContentValues();

        contentValues.put("toiletname", toiletInfo.toiletName);
        contentValues.put("district", toiletInfo.district);
        contentValues.put("municipality", toiletInfo.municipality);
        contentValues.put("address", toiletInfo.address);
        contentValues.put("latitude", toiletInfo.latitude);
        contentValues.put("longitude", toiletInfo.longitude);
        contentValues.put("availabletime", toiletInfo.availableTime);
        contentValues.put("nearbysightseeing", toiletInfo.nearbySightseeing);
        contentValues.put("lastupdatedate", java.lang.System.currentTimeMillis());
        db.insert("toiletinfo", null, contentValues);
    }
    public ArrayList<ToiletInfoModel> search(SQLiteDatabase db){
        ArrayList<ToiletInfoModel> aryToiletInfo = new ArrayList<ToiletInfoModel>();
        Cursor cursor = db.query("toiletinfo", null, null, null, null, null, "id desc", null);
        int count = cursor.getCount();
        cursor.moveToFirst();

        for(int i = 0; i < count; i++){
            ToiletInfoModel toiletInfoModel = new ToiletInfoModel();
            toiletInfoModel.toiletName = cursor.getString(cursor.getColumnIndex("toiletname"));
            toiletInfoModel.district = cursor.getString(cursor.getColumnIndex("district"));
            toiletInfoModel.municipality = cursor.getString(cursor.getColumnIndex("municipality"));
            toiletInfoModel.address = cursor.getString(cursor.getColumnIndex("address"));
            toiletInfoModel.latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            toiletInfoModel.longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            toiletInfoModel.availableTime = cursor.getString(cursor.getColumnIndex("availabletime"));
            toiletInfoModel.nearbySightseeing = cursor.getString(cursor.getColumnIndex("nearbysightseeing"));

            aryToiletInfo.add(toiletInfoModel);
            cursor.moveToNext();
        }

        return aryToiletInfo;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS toiletinfo("
                        + "id INTEGER PRIMARY KEY, "
                        + "toiletname TEXT NOT NULL, "
                        + "district TEXT, "
                        + "municipality TEXT, "
                        + "address TEXT NOT NULL, "
                        + "latitude REAL NOT NULL, "
                        + "longitude REAL NOT NULL, "
                        + "availabletime TEXT, "
                        + "nearbysightseeing TEXT, "
                        + "lastupdatedate INTEGER)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int intOldVer, int intNewVer)
    {

    }
}

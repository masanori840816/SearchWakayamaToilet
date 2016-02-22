package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2015/12/31.
 * this class manages DB.
 */

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseAccesser extends SQLiteOpenHelper{
    public class ToiletInfoModel{
        public int id = 0;
        public String toiletName;
        public String toiletEnglishName;
        public String toiletChineseName;
        public String district;
        public String municipality;
        public String address;
        public String englishAddress;
        public String chineseAddress;
        public double latitude;
        public double longitude;
        public String availableTime;
        public boolean hasMultiPurposeToilet;
    }
    private String strSearchCriteria;
    private String[] strSearchParameters;

    public DatabaseAccesser(Context context){
        super(context, "toiletinfo.db", null, 1);
    }
    public void insertInfo(SQLiteDatabase db, ToiletInfoModel toiletInfo){

        // Transactionの開始・終了は呼び出し元で実行.
        ContentValues contentValues = new ContentValues();

        contentValues.put("toiletname", toiletInfo.toiletName);
        contentValues.put("toiletenglishname", toiletInfo.toiletEnglishName);
        contentValues.put("toiletchinesename", toiletInfo.toiletChineseName);
        contentValues.put("district", toiletInfo.district);
        contentValues.put("municipality", toiletInfo.municipality);
        contentValues.put("address", toiletInfo.address);
        contentValues.put("englishaddress", toiletInfo.englishAddress);
        contentValues.put("chineseaddress", toiletInfo.chineseAddress);
        contentValues.put("latitude", toiletInfo.latitude);
        contentValues.put("longitude", toiletInfo.longitude);
        contentValues.put("availabletime", toiletInfo.availableTime);
        contentValues.put("lastupdatedate", java.lang.System.currentTimeMillis());
        db.insert("toiletinfo", null, contentValues);
    }
    public void setSearchCriteriaFromFreeWord(String strWord){
        // とりあえずAnd検索のみ.
        String[] splittedWords = strWord.split("　|\\s");
        StringBuilder _newSearchCriteria = new StringBuilder();

        // toiletname, addressが対象.
        strSearchParameters = new String[splittedWords.length * 2];

        for(int i = 0, j = 0; i < splittedWords.length; i++){
            if(i > 0){
                _newSearchCriteria.append(" AND ");
            }
            _newSearchCriteria.append("(toiletname LIKE ? OR address LIKE ?)");

            StringBuilder _newParameter = new StringBuilder("%");
            _newParameter.append(splittedWords[i]);
            _newParameter.append("%");
            strSearchParameters[j] = _newParameter.toString();
            strSearchParameters[j + 1] = _newParameter.toString();
            j += 2;
        }

        strSearchCriteria = _newSearchCriteria.toString();
    }
    public ArrayList<ToiletInfoModel> search(SQLiteDatabase db){
        ArrayList<ToiletInfoModel> aryToiletInfo = new ArrayList();

        Cursor _cursor = db.query("toiletinfo", null, strSearchCriteria, strSearchParameters, null, null, "id ASC", null);


        _cursor.moveToFirst();

        for(int i = 0; i < _cursor.getCount(); i++){
            ToiletInfoModel toiletInfoModel = new ToiletInfoModel();
            toiletInfoModel.toiletName = _cursor.getString(_cursor.getColumnIndex("toiletname"));
            toiletInfoModel.toiletEnglishName = _cursor.getString(_cursor.getColumnIndex("toiletenglishname"));
            toiletInfoModel.toiletChineseName = _cursor.getString(_cursor.getColumnIndex("toiletchinesename"));
            toiletInfoModel.district = _cursor.getString(_cursor.getColumnIndex("district"));
            toiletInfoModel.municipality = _cursor.getString(_cursor.getColumnIndex("municipality"));
            toiletInfoModel.address = _cursor.getString(_cursor.getColumnIndex("address"));
            toiletInfoModel.englishAddress = _cursor.getString(_cursor.getColumnIndex("englishaddress"));
            toiletInfoModel.chineseAddress = _cursor.getString(_cursor.getColumnIndex("chineseaddress"));
            toiletInfoModel.latitude = _cursor.getDouble(_cursor.getColumnIndex("latitude"));
            toiletInfoModel.longitude = _cursor.getDouble(_cursor.getColumnIndex("longitude"));
            toiletInfoModel.availableTime = _cursor.getString(_cursor.getColumnIndex("availabletime"));

            aryToiletInfo.add(toiletInfoModel);
            _cursor.moveToNext();
        }
        // 検索後はNullに戻す.
        strSearchCriteria = null;
        strSearchParameters = null;

        return aryToiletInfo;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS toiletinfo("
                        + "id INTEGER PRIMARY KEY, "
                        + "toiletname TEXT NOT NULL, "
                        + "toiletenglishname TEXT, "
                        + "toiletchinesename TEXT, "
                        + "district TEXT, "
                        + "municipality TEXT, "
                        + "address TEXT NOT NULL, "
                        + "englishaddress TEXT NOT NULL, "
                        + "chineseaddress TEXT NOT NULL, "
                        + "latitude REAL NOT NULL, "
                        + "longitude REAL NOT NULL, "
                        + "availabletime TEXT, "
                        + "lastupdatedate INTEGER)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int intOldVer, int intNewVer)
    {
        db.execSQL("DROP TABLE IF EXISTS toiletinfo");
        this.onCreate(db);
    }
}

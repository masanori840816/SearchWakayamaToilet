package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/07/27.
 * data model.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class ToiletInfoModel extends SQLiteOpenHelper {
    private String searchCriteria;
    private String[] searchParameters;

    public ToiletInfoModel(Context context){
        super(context, "toiletinfo.db", null, 4);
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
                + "hasMultiPurposeToilet NUMERIC)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersionNum, int newVersionNum) {
        db.execSQL("DROP TABLE IF EXISTS toiletinfo");
        db.execSQL("DROP TABLE IF EXISTS toiletaltname");
        this.onCreate(db);
    }
    public void  insertInfo(SQLiteDatabase db, ToiletInfoClass toiletInfoClass) {
        Cursor cursorMaxId = db.query("toiletinfo"
                , new String[]{"MAX(id)"}
                , null
                , null
                , null, null, null);
        cursorMaxId.moveToFirst();
        int maxId = cursorMaxId.getInt(cursorMaxId.getColumnIndex("MAX(id)"));
        cursorMaxId.close();


        for(ToiletInfoClass.ToiletInfo toiletInfo: toiletInfoClass.getToiletInfoList()){
            Cursor cursorExistence = db.query("toiletinfo"
                    , new String[]{"id"}
                    , "toiletname = ? AND address = ?"
                    , new String[]{toiletInfo.toiletName, toiletInfo.address}
                    , null, null, null);
            cursorExistence.moveToFirst();

            if(cursorExistence.getCount() > 0){
                // 既存データをアップデート.
                update(db, toiletInfo, cursorExistence.getInt(cursorExistence.getColumnIndex("id")));
            }
            else{
                // 新規追加.
                maxId++;
                insert(db, toiletInfo, maxId);
            }
            cursorExistence.close();
        }
    }
    public void setSearchCriteriaFromFreeWord(String inputWord) {
        // とりあえずAnd検索のみ.
        String[] splittedWords;

        if(inputWord == null) {
            searchCriteria = null;
            searchParameters = null;
            return;
        }
        splittedWords = inputWord.split("　|\\s");

        StringBuilder newSearchCriteria = new StringBuilder();

        searchParameters = new String[splittedWords.length * 2];

        int t = 0;
        for(int i = 0; i < splittedWords.length; i++){
            if (i > 0) {
                newSearchCriteria.append(" AND ");
            }
            newSearchCriteria.append("(toiletname LIKE ? OR address LIKE ?)");

            StringBuilder newParameter = new StringBuilder("%");
            newParameter.append(splittedWords[i]);
            newParameter.append("%");
            // toiletname, addressが対象.
            searchParameters[t] = newParameter.toString();
            t++;
            searchParameters[t] = newParameter.toString();
            t++;
        }

        searchCriteria = newSearchCriteria.toString();
    }
    public ToiletInfoClass search(SQLiteDatabase db) {
        ToiletInfoClass toiletInfoClass = new ToiletInfoClass();
        ArrayList<ToiletInfoClass.ToiletInfo> resultList = new ArrayList<>();

        Cursor cursor = db.query("toiletinfo", null, searchCriteria, searchParameters, null, null, "id ASC", null);

        cursor.moveToFirst();

        for(int i = 0; i < cursor.getCount(); i++){
            ToiletInfoClass.ToiletInfo newInfo = toiletInfoClass.new ToiletInfo();
            newInfo.toiletName = cursor.getString(cursor.getColumnIndex("toiletname"));
            newInfo.district = cursor.getString(cursor.getColumnIndex("district"));
            newInfo.municipality = cursor.getString(cursor.getColumnIndex("municipality"));
            newInfo.address = cursor.getString(cursor.getColumnIndex("address"));
            newInfo.latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            newInfo.longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            newInfo.availableTime = cursor.getString(cursor.getColumnIndex("availabletime"));
            newInfo.hasMultiPurposeToilet = cursor.getInt(cursor.getColumnIndex("hasMultiPurposeToilet")) == 1;

            resultList.add(newInfo);
            cursor.moveToNext();
        }
        cursor.close();
        toiletInfoClass.setToiletInfoList(resultList);
        // 検索後は空に戻す.
        searchCriteria = "";
        searchParameters = new String[0];

        return toiletInfoClass;
    }

    private void insert(SQLiteDatabase db, ToiletInfoClass.ToiletInfo toiletInfo, int newId){
        // Transactionの開始.
        db.beginTransaction();

        // Transactionの開始・終了は呼び出し元で実行.
        ContentValues contentValues = new ContentValues();

        contentValues.put("id", newId);
        contentValues.put("toiletname", toiletInfo.toiletName);
        contentValues.put("district", toiletInfo.district);
        contentValues.put("municipality", toiletInfo.municipality);
        contentValues.put("address", toiletInfo.address);
        contentValues.put("latitude", toiletInfo.latitude);
        contentValues.put("longitude", toiletInfo.longitude);
        contentValues.put("availabletime", toiletInfo.availableTime);
        contentValues.put("hasMultiPurposeToilet", toiletInfo.hasMultiPurposeToilet);
        db.insert("toiletinfo", null, contentValues);

        // CommitしてTransactionを終了.

        db.setTransactionSuccessful();
        db.endTransaction();
    }
    private void update(SQLiteDatabase db, ToiletInfoClass.ToiletInfo toiletInfo, int targetId){
        // Transactionの開始.
        db.beginTransaction();

        // Transactionの開始・終了は呼び出し元で実行.
        ContentValues contentValues = new ContentValues();

        contentValues.put("id", targetId);
        contentValues.put("toiletname", toiletInfo.toiletName);
        contentValues.put("district", toiletInfo.district);
        contentValues.put("municipality", toiletInfo.municipality);
        contentValues.put("address", toiletInfo.address);
        contentValues.put("latitude", toiletInfo.latitude);
        contentValues.put("longitude", toiletInfo.longitude);
        contentValues.put("availabletime", toiletInfo.availableTime);
        contentValues.put("hasMultiPurposeToilet", toiletInfo.hasMultiPurposeToilet);

        db.update("toiletinfo", contentValues, "id = ?", new String[]{String.valueOf(targetId)});
        // CommitしてTransactionを終了.

        db.setTransactionSuccessful();
        db.endTransaction();
    }
}

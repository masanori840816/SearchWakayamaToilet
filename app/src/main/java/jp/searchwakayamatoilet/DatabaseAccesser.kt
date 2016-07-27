package jp.searchwakayamatoilet

/**
 * Created by masanori on 2015/12/31.
 * this class manages DB.
 */

import android.content.Context
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import java.util.ArrayList

class DatabaseAccesser(context: Context) : SQLiteOpenHelper(context, "toiletinfo.db", null, 3) {
    data class ToiletInfoClass(var id: Int = 0, var toiletName: String, var district: String
            , var municipality: String, var address: String, var latitude: Double, var longitude: Double
            , var availableTime: String, var hasMultiPurposeToilet: Boolean)

    private var strSearchCriteria: String? = null
    private var arySearchParameter: Array<String> = emptyArray()
    fun insertInfo(db: SQLiteDatabase, toiletInfo: ToiletInfoClass) {

        // Transactionの開始・終了は呼び出し元で実行.
        val contentValues = ContentValues()

        contentValues.put("toiletname", toiletInfo.toiletName)
        contentValues.put("district", toiletInfo.district)
        contentValues.put("municipality", toiletInfo.municipality)
        contentValues.put("address", toiletInfo.address)
        contentValues.put("latitude", toiletInfo.latitude)
        contentValues.put("longitude", toiletInfo.longitude)
        contentValues.put("availabletime", toiletInfo.availableTime)
        contentValues.put("hasMultiPurposeToilet", toiletInfo.hasMultiPurposeToilet)
        contentValues.put("lastupdatedate", java.lang.System.currentTimeMillis())
        db.insert("toiletinfo", null, contentValues)
    }

    fun setSearchCriteriaFromFreeWord(strWord: String?) {
        // とりあえずAnd検索のみ.
        val splittedWords = strWord?.split("　|\\s".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        if(splittedWords == null){
            return
        }
        val _newSearchCriteria = StringBuilder()

        var i = 0
        var j = 0
        arySearchParameter = Array(splittedWords.size * 2, {""})

        while (i < splittedWords.size) {
            if (i > 0) {
                _newSearchCriteria.append(" AND ")
            }
            _newSearchCriteria.append("(toiletname LIKE ? OR address LIKE ?)")

            val _newParameter = StringBuilder("%")
            _newParameter.append(splittedWords[i])
            _newParameter.append("%")
            // toiletname, addressが対象.
            arySearchParameter.set(j, _newParameter.toString())
            j++
            arySearchParameter.set(j, _newParameter.toString())
            j++

            i++
        }

        strSearchCriteria = _newSearchCriteria.toString()
    }

    fun search(db: SQLiteDatabase): ArrayList<ToiletInfoClass> {
        val aryToiletInfo = ArrayList<ToiletInfoClass>()

        val _cursor = db.query("toiletinfo", null, strSearchCriteria, arySearchParameter, null, null, "id ASC", null)

        _cursor.moveToFirst()

        for (i in 0.._cursor.count - 1) {
            val toiletInfoModel = ToiletInfoClass(
                    toiletName = _cursor.getString(_cursor.getColumnIndex("toiletname"))
                    , district = _cursor.getString(_cursor.getColumnIndex("district"))
                    , municipality = _cursor.getString(_cursor.getColumnIndex("municipality"))
                    , address = _cursor.getString(_cursor.getColumnIndex("address"))
                    , latitude = _cursor.getDouble(_cursor.getColumnIndex("latitude"))
                    , longitude = _cursor.getDouble(_cursor.getColumnIndex("longitude"))
                    , availableTime = _cursor.getString(_cursor.getColumnIndex("availabletime"))
                    , hasMultiPurposeToilet = _cursor.getInt(_cursor.getColumnIndex("hasMultiPurposeToilet")) == 1)

            aryToiletInfo.add(toiletInfoModel)
            _cursor.moveToNext()
        }
        _cursor.close()
        // 検索後はNullに戻す.
        strSearchCriteria = null
        arySearchParameter = emptyArray()

        return aryToiletInfo
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS toiletinfo("
                + "id INTEGER PRIMARY KEY, "
                + "toiletname TEXT NOT NULL, "
                + "district TEXT, "
                + "municipality TEXT, "
                + "address TEXT NOT NULL, "
                + "latitude REAL NOT NULL, "
                + "longitude REAL NOT NULL, "
                + "availabletime TEXT, "
                + "hasMultiPurposeToilet NUMERIC, "
                + "lastupdatedate INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS toiletaltname("
                + "toiletid INTEGER NOT NULL, "
                + "language TEXT NOT NULL, "
                + "alttoiletname TEXT NOT NULL, "
                + "altdistrict TEXT, "
                + "altmunicipality TEXT, "
                + "altaddress"
                + "lastupdatedate INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, intOldVer: Int, intNewVer: Int) {
        db.execSQL("DROP TABLE IF EXISTS toiletinfo")
        db.execSQL("DROP TABLE IF EXISTS toiletaltname")
        this.onCreate(db)
    }
}

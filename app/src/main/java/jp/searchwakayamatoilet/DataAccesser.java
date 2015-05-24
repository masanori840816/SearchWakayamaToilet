package jp.searchwakayamatoilet;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataAccesser extends SQLiteOpenHelper{
    public DataAccesser(Context context) {
        super(context, "toiletaddress.db", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS ToiletAddress("
            + "ToiletID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "ToiletName TEXT NOT NULL, "
            + "AreaName TEXT NOT NULL, "
            + "Address TEXT NOT NULL, "
            + "Latitude REAL NOT NULL, "
            + "Longitude REAL NOT NULL, "
            + "LastUpdateDate DATETIME)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int intOldVer, int intNewVer)
    {

    }
}

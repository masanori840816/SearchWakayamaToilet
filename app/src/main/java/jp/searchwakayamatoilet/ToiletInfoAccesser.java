package jp.searchwakayamatoilet;
/**
 * Created by masanori on 2016/07/27.
 * this class accesses ToiletInfoModel.
 */

import android.app.Activity;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import rx.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class ToiletInfoAccesser {
    @NonNull
    private ToiletInfoModel toiletInfoModel;
    @NonNull
    private SQLiteDatabase sqlite;

    public ToiletInfoAccesser(Activity currentActivity){
        toiletInfoModel = new ToiletInfoModel(currentActivity);
        sqlite = toiletInfoModel.getWritableDatabase();
    }
    public Observable<ArrayList<ToiletInfoClass.ToiletInfo>> loadToiletData(Activity currentActivity, boolean isExistingDataUsed, String query) {
        // Subscriber<? ToiletInfoModel> subscriber.
        return Observable.create(
                subscriber -> {
                    toiletInfoModel.setSearchCriteriaFromFreeWord(query);

                    if (isExistingDataUsed){
                        // 既存データを使う場合はDBを検索してマーカーをセット.
                        ToiletInfoClass toiletInfoClass = toiletInfoModel.search(sqlite);
                        subscriber.onNext(toiletInfoClass.getToiletInfoList());
                        subscriber.onCompleted();
                    }
                    else{
                        AssetManager assetManager = currentActivity.getResources().getAssets();
                        if(assetManager == null){
                            subscriber.onCompleted();
                        }
                        else{
                            try {
                                // Jsonの読み込み.
                                InputStream inputStream = assetManager.open("toiletdata.json");
                                JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));

                                ToiletInfoClass jsonToiletInfoClass = new Gson().fromJson(jsonReader, ToiletInfoClass.class);

                                // 取得したデータをDBに挿入.
                                toiletInfoModel.insertInfo(sqlite, jsonToiletInfoClass);

                                jsonReader.close();
                                inputStream.close();

                                // DBへのデータ挿入後、データを検索してマーカー設置.
                                ToiletInfoClass toiletInfoClass = toiletInfoModel.search(sqlite);
                                subscriber.onNext(toiletInfoClass.getToiletInfoList());
                                subscriber.onCompleted();
                            } catch (IOException ex) {
                                subscriber.onError(ex);
                            }
                        }
                    }
                });
    }
    public Observable<ArrayList<ToiletInfoClass.ToiletInfo>> searchToiletData(String query) {
        return Observable.create(
                // Subscriber<? ToiletInfoModel> subscriber.
                subscriber -> {
                    toiletInfoModel.setSearchCriteriaFromFreeWord(query);

                    // Add marker on UI Thread.
                    subscriber.onNext(toiletInfoModel.search(toiletInfoModel.getWritableDatabase()).getToiletInfoList());
                    subscriber.onCompleted();
                });
    }
}

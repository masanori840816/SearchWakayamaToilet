package jp.searchwakayamatoilet;
/**
 * Created by masanori on 2016/07/27.
 * this class accesses ToiletInfoModel.
 */

import android.app.Activity;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import rx.Observable;
import com.eccyan.optional.Optional;

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
                    ToiletInfoClass toiletInfoClass = toiletInfoModel.search(sqlite);

                    if (isExistingDataUsed
                        && toiletInfoClass.getInfoCount() > 0){
                        // 既存データがDBに存在すればそのままマーカーをセット.
                        subscriber.onNext(toiletInfoClass.getToiletInfoList());
                        subscriber.onCompleted();
                    }
                    else{
                        Optional<AssetManager> assetManager = Optional.of(currentActivity.getResources().getAssets());
                        assetManager.ifPresent(asset -> {
                            try {
                                try {
                                    // CSVの読み込み.
                                    InputStream inputStream = asset.open("toiletdata.json");
                                    JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));

                                    ToiletInfoClass jsonToiletInfoClass = new Gson().fromJson(jsonReader, ToiletInfoClass.class);

                                    // 取得したデータをDBに挿入.
                                    toiletInfoModel.insertInfo(sqlite, jsonToiletInfoClass);

                                    jsonReader.close();
                                    inputStream.close();


                                    // Add marker on UI Thread.

                                    // TODO: 挿入完了後にDBを再検索したデータに置き換え.
                                    subscriber.onNext(jsonToiletInfoClass.getToiletInfoList());
                                    subscriber.onCompleted();
                                } catch (IOException ex) {
                                    subscriber.onError(ex);
                                }
                            }catch (SQLiteException ex) {
                                subscriber.onError(ex);
                            }
                        });
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

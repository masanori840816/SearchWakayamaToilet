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
import android.util.Log;
import rx.Observable;
import com.eccyan.optional.Optional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import jp.searchwakayamatoilet.DatabaseAccesser.ToiletInfoClass;

public class ToiletInfoAccesser {
    @NonNull
    private DatabaseAccesser toiletInfoModel;
    @NonNull
    private SQLiteDatabase sqlite;
    private boolean isTransactionStarted;
    private boolean isLoadingCancelled;

    public ToiletInfoAccesser(Activity currentActivity){
        toiletInfoModel = new DatabaseAccesser(currentActivity);
        sqlite = toiletInfoModel.getWritableDatabase();
    }
    public Observable<List<ToiletInfoClass>> loadToiletData(Activity currentActivity, boolean isExistingDataUsed, String query) {
        // Subscriber<? ToiletInfoModel> subscriber.
        return Observable.create(
                subscriber -> {
                    toiletInfoModel.setSearchCriteriaFromFreeWord(query);
                    List<ToiletInfoClass> toiletInfoList = toiletInfoModel.search(sqlite);
                    isTransactionStarted = false;

                    if (isExistingDataUsed
                        && toiletInfoList.size() > 0){
                        // 既存データがDBに存在すればそのままマーカーをセット.
                        subscriber.onNext(toiletInfoList);
                        subscriber.onCompleted();
                    }
                    else{
                        Optional<AssetManager> assetManager = Optional.of(currentActivity.getResources().getAssets());
                        assetManager.ifPresent(asset -> {
                            try {
                                try {
                                    // CSVの読み込み.
                                    InputStreamReader inputStreamReader = new InputStreamReader(asset.open("toilet-map-edited.csv"));
                                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                    toiletInfoList.clear();

                                    // 1行ずつ読み込む.
                                    while (true) {
                                        if (isLoadingCancelled) {
                                            if (isTransactionStarted) {
                                                // CommitしてTransactionを終了.
                                                sqlite.setTransactionSuccessful();
                                                sqlite.endTransaction();
                                            }
                                            break;
                                        }
                                        String newLineText = bufferedReader.readLine();
                                        if(newLineText == null
                                                || newLineText.isEmpty()){
                                            break;
                                        }
                                        String[] splittedLineText = newLineText.split(",");
                                        // Split後に8件データがあればDBに登録.
                                        if(splittedLineText.length != 8){
                                            continue;
                                        }
                                        // 0:No, 1:名称, 2:市町村名, 3:住所, 4: 緯度, 5:経度, 6:利用時間, 7: 多目的トイレの有無.
                                        ToiletInfoClass toiletInfo = new ToiletInfoClass(
                                                splittedLineText[1]
                                                // 都道府県は和歌山固定.
                                                , "和歌山"
                                                , splittedLineText[2]
                                                , splittedLineText[3]
                                                , tryParseToDouble(splittedLineText[4])
                                                , tryParseToDouble(splittedLineText[5])
                                                , splittedLineText[6]
                                                , Boolean.valueOf(splittedLineText[7]));

                                        ToiletInfoClass checker = Observable.from(toiletInfoList)
                                                .filter(info -> info.getToiletName().equals(splittedLineText[1])
                                                            && info.getAddress().equals(splittedLineText[3]))
                                                .toBlocking()
                                                .firstOrDefault(null);

                                        if(checker == null){
                                            toiletInfoList.add(toiletInfo);
                                            if (isTransactionStarted) {
                                                // Transactionの開始.
                                                sqlite.beginTransaction();
                                                isTransactionStarted = true;
                                            }

                                            // if there is no same data, insert as new one.
                                            toiletInfoModel.insertInfo(sqlite, toiletInfo);
                                        }
                                        if (isTransactionStarted) {
                                            // CommitしてTransactionを終了.
                                            sqlite.setTransactionSuccessful();
                                            sqlite.endTransaction();
                                        }

                                        bufferedReader.close();
                                        inputStreamReader.close();

                                        // Add marker on UI Thread.
                                        subscriber.onNext(toiletInfoList);
                                        subscriber.onCompleted();
                                    }
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
    public Observable<List<ToiletInfoClass>> searchToiletData(String query) {
        return Observable.create(
                // Subscriber<? ToiletInfoModel> subscriber.
                subscriber -> {
                    toiletInfoModel.setSearchCriteriaFromFreeWord(query);

                    // Add marker on UI Thread.
                    subscriber.onNext(toiletInfoModel.search(toiletInfoModel.getWritableDatabase()));
                    subscriber.onCompleted();
                });
    }
    public void stopLoading() {
        // TODO: 非同期処理の方法を調べて修正.
        isLoadingCancelled = true;
    }
    public void onCancelled() {
        if (isTransactionStarted) {
            sqlite.setTransactionSuccessful();
            sqlite.endTransaction();
        }
    }
    private double tryParseToDouble(@NonNull String originalText){
        try {
            return Double.parseDouble(originalText);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}

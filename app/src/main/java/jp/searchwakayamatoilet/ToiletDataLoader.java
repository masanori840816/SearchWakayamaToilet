package jp.searchwakayamatoilet;

import android.app.Activity;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

import com.annimon.stream.Stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by masanori on 2016/01/24.
 */
public class ToiletDataLoader extends AsyncTask<Void, Void, Integer> {

    private DatabaseAccesser dbAccesser;
    private SQLiteDatabase sqlite;
    private boolean isExistingDataUsed;
    private boolean isTransactionStarted;
    private Activity currentActivity;
    private MainPresenter currentPresenter;
    private boolean isLoadingCancelled;
    private String strQuery;

    public ToiletDataLoader(Activity newActivity, boolean newIsExistingDataUsed, MainPresenter newPresenter, String newQuery){
        currentPresenter = newPresenter;
        currentActivity = newActivity;
        isExistingDataUsed = newIsExistingDataUsed;

        dbAccesser = new DatabaseAccesser(newActivity);
        sqlite = dbAccesser.getWritableDatabase();
        isLoadingCancelled = false;
        strQuery = newQuery;
    }
    public void stopLoading(){
        isLoadingCancelled = true;
    }


    @Override
    protected Integer doInBackground(Void... params) {
        dbAccesser.setSearchCriteriaFromFreeWord(strQuery);
        final ArrayList<DatabaseAccesser.ToiletInfoModel> aryToiletInfo = dbAccesser.search(sqlite);
        isTransactionStarted = false;

        if (isExistingDataUsed
                && aryToiletInfo != null
                && aryToiletInfo.size() > 0) {
            // Add marker on UI Thread.
            currentPresenter.addMarker(aryToiletInfo);
        } else {
            // Runnable() - run().
            currentPresenter.startLoadingCsvData();

            AssetManager asmAsset = currentActivity.getResources().getAssets();
            try {
                // CSVの読み込み.
                InputStream ipsInput = asmAsset.open("toilet-map-edited.csv");
                InputStreamReader inputStreamReader = new InputStreamReader(ipsInput);
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                String strLine = "";
                String[] strSplited;
                Pattern patternNum = Pattern.compile("^[0-9]+");

                ArrayList<DatabaseAccesser.ToiletInfoModel> aryLoadedToiletInfo = new ArrayList<>();
                // 1行ずつ読み込む.
                while (true) {
                    if(isLoadingCancelled){
                        if(isTransactionStarted) {
                            // CommitしてTransactionを終了.
                            sqlite.setTransactionSuccessful();
                            sqlite.endTransaction();
                        }
                        break;
                    }

                    strLine = bufferReader.readLine();
                    if(strLine == null){
                        // stop loading if the line is empty.
                        break;
                    }

                    // if the line isn't start with number, skip loading.
                    if (! patternNum.matcher(strLine).find()) {
                        continue;
                    }
                    // Split後に9件データがある行のみ.
                    strSplited = strLine.split(",");

                    if (strSplited.length >= 9) {
                        // 0:No, 1:名称, 2:名称(英語), 3:名称(中国語), 4:市町村名, 5:住所, 6:住所(英語), 7:住所(中国語)
                        // ,8: 緯度, 9:経度, 10:利用時間.
                        DatabaseAccesser.ToiletInfoModel _toiletInfoModel = dbAccesser.new ToiletInfoModel();
                        _toiletInfoModel.toiletName = strSplited[1];
                        _toiletInfoModel.toiletEnglishName = strSplited[2];
                        _toiletInfoModel.toiletChineseName = strSplited[3];
                        // 都道府県は和歌山固定.
                        _toiletInfoModel.district = "和歌山";
                        _toiletInfoModel.municipality = strSplited[4];
                        _toiletInfoModel.address = strSplited[5];
                        _toiletInfoModel.englishAddress = strSplited[6];
                        _toiletInfoModel.chineseAddress = strSplited[7];
                        _toiletInfoModel.latitude = tryParseToDouble(strSplited[8]);
                        _toiletInfoModel.longitude = tryParseToDouble(strSplited[9]);
                        _toiletInfoModel.availableTime = strSplited[10];

                        aryLoadedToiletInfo.add(_toiletInfoModel);

                        final String _toiletName = _toiletInfoModel.toiletName;
                        final String _toiletAddress = _toiletInfoModel.address;
                        long lngCount = Stream.of(aryToiletInfo)
                                .filter(toiletInfo -> (toiletInfo.toiletName.equals(_toiletName)
                                        && toiletInfo.address.equals(_toiletAddress))).count();

                        if (lngCount <= 0) {
                            if (isTransactionStarted) {
                                // Transactionの開始.
                                sqlite.beginTransaction();
                                isTransactionStarted = true;
                            }

                            // if there is no same data, insert as new one.
                            dbAccesser.insertInfo(sqlite, _toiletInfoModel);
                        }
                        _toiletInfoModel = null;
                    }
                }
                if(isTransactionStarted) {
                    // CommitしてTransactionを終了.
                    sqlite.setTransactionSuccessful();
                    sqlite.endTransaction();
                }

                bufferReader.close();
                ipsInput.close();

                // Add marker on UI Thread.
                currentPresenter.addMarker(aryLoadedToiletInfo);

                currentPresenter.stopLoadingCsvData();

            } catch (IOException ex) {
                currentPresenter.showErrorDialog(ex.getLocalizedMessage());
            } catch (SQLiteException ex) {
                currentPresenter.showErrorDialog(ex.getLocalizedMessage());
            }
        }
        return 0;
    }
    private double tryParseToDouble(String numberString){
        try{
            double numberDouble = Double.parseDouble(numberString);
            return numberDouble;
        }catch(NumberFormatException e){
            return 0;
        }
    }
    @Override
    protected void onPostExecute(Integer result) {
    }
    @Override
    protected void onCancelled(){
        if(isTransactionStarted){
            sqlite.setTransactionSuccessful();
            sqlite.endTransaction();
        }
    }
}

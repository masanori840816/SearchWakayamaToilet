package jp.searchwakayamatoilet;

import android.app.Activity;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.annimon.stream.Stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by masanori on 2016/01/24.
 */
public class ToiletDataLoader extends AsyncTask<Integer, Integer, Integer> {

    private DatabaseAccesser dbAccesser;
    private SQLiteDatabase sqlite;
    private boolean isExistingDataUsed;
    private boolean isTransactionStarted;
    private Activity currentActivity;
    private IPageView currentView;
    private boolean isDataLoading;
    private NetworkAccesser networkAccesser;
    private LoadingPanelViewer loadingPanelViewer;

    private DatabaseAccesser.ToiletInfoModel toiletInfoModel;

    public void setIsExistingDataUsed(boolean newValue){
        isExistingDataUsed = newValue;
    }
    public ToiletDataLoader(Activity newActivity, IPageView newView){
        super();
        currentActivity = newActivity;
        currentView = newView;

        dbAccesser = new DatabaseAccesser(newActivity);
        sqlite = dbAccesser.getWritableDatabase();
        toiletInfoModel = dbAccesser.new ToiletInfoModel();
        networkAccesser = new NetworkAccesser();
        loadingPanelViewer = new LoadingPanelViewer(newActivity, newView);
    }

    @Override
    protected Integer doInBackground(Integer... integer) {
        final ArrayList<DatabaseAccesser.ToiletInfoModel> aryToiletInfo = dbAccesser.search(sqlite);
        isTransactionStarted = false;

        if (isExistingDataUsed
                && aryToiletInfo != null
                && aryToiletInfo.size() > 0) {
            for (DatabaseAccesser.ToiletInfoModel toiletInfo : aryToiletInfo) {

                // Add marker on UI Thread.
                // Runnable() - run().
                currentActivity.runOnUiThread(
                        () -> {
                            currentView.addMarker(toiletInfo.toiletName, toiletInfo.latitude, toiletInfo.longitude);
                        });
            }
        } else if (networkAccesser.checkIsNetworkConnected(currentActivity)) {
            isDataLoading = true;
            // Runnable() - run().
            currentActivity.runOnUiThread(
                    () -> {
                        // show loading dialog.
                        loadingPanelViewer.show();
                    });
            Geocoder geocoder = new Geocoder(currentActivity, Locale.getDefault());
            AssetManager asmAsset = currentActivity.getResources().getAssets();
            try {
                // CSVの読み込み.
                InputStream ipsInput = asmAsset.open("toilet-map.csv");
                InputStreamReader inputStreamReader = new InputStreamReader(ipsInput);
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                String strLine = "";
                String[] strSplited;
                Pattern p = Pattern.compile("^[0-9]+");

                // Transactionの開始.
                sqlite.beginTransaction();
                isTransactionStarted = true;

                // 1行ずつ読み込む.
                while (bufferReader.readLine() != null) {
                    strLine = bufferReader.readLine();

                    // とりあえず数値から始まっている行のみ
                    if (p.matcher(strLine).find()) {
                        // とりあえずSplit後に4件以上データがある行のみ.
                        strSplited = strLine.split(",");
                        if (strSplited.length >= 4) {

                            List addressList = geocoder.getFromLocationName(strSplited[3], 1);
                            if (!addressList.isEmpty()) {

                                Address address = (Address) addressList.get(0);

                                toiletInfoModel.toiletName = strSplited[1];
                                // 都道府県はひとまず和歌山固定.
                                toiletInfoModel.district = "和歌山";
                                toiletInfoModel.municipality = strSplited[2];
                                toiletInfoModel.address = strSplited[3];
                                toiletInfoModel.latitude = address.getLatitude();
                                toiletInfoModel.longitude = address.getLongitude();
                                toiletInfoModel.availableTime = strSplited[4];
                                String strNearbySightseeing = (strSplited.length > 35) ? strSplited[35] : "";
                                toiletInfoModel.nearbySightseeing = strNearbySightseeing;

                                // Add marker on UI Thread.
                                // Runnable() - run().
                                currentActivity.runOnUiThread(
                                        () -> {
                                            currentView.addMarker(toiletInfoModel.toiletName, toiletInfoModel.latitude, toiletInfoModel.longitude);
                                        });

                                final String toiletName = strSplited[1];
                                final String toiletAddress = strSplited[3];
                                long lngCount = Stream.of(aryToiletInfo)
                                        .filter(toiletInfo -> (toiletInfo.toiletName.equals(toiletName)
                                                && toiletInfo.address.equals(toiletAddress))).count();

                                if (lngCount <= 0) {
                                    // if there is no same data, insert as new one.
                                    dbAccesser.insertInfo(sqlite, toiletInfoModel);
                                }
                            }
                        }
                    }
                }
                // CommitしてTransactionを終了.
                sqlite.setTransactionSuccessful();
                sqlite.endTransaction();

                bufferReader.close();
                ipsInput.close();
                isDataLoading = false;

                // Runnable() - run().
                currentActivity.runOnUiThread(
                        () -> {
                            // hide Loading Dialog.
                            loadingPanelViewer.hide();
                        }
                );

            } catch (IOException e) {
                // TODO: error処理.
                Log.d("SWT", "Exception:" + e.getLocalizedMessage());
            } catch (SQLiteException e) {
                // TODO: error処理.
                Log.d("SWT", "Exception:" + e.getLocalizedMessage());
            }
        }
        return 0;
    }
    @Override
    protected void onPostExecute(Integer result) {
        Log.d("SWT", "postExecute");
    }
    @Override
    protected void onCancelled(){
        if(isTransactionStarted){

        }
    }
}

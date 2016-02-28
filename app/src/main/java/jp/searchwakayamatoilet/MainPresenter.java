package jp.searchwakayamatoilet;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import java.util.ArrayList;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by masanori on 2016/01/24.
 */
public class MainPresenter {
    //private Activity currentActivity;
    private final FragmentActivity currentActivity;
    private final LocationAccesser locationAccesser;
    private final LoadingPanelViewer loadingPanelViewer;
    private ToiletDataLoader dataLoader;
    private ToiletDataSearcher dataSearcher;
    private boolean isLoadingCanceled;

    private TimerController timeController;
    private Timer tmrGettingLocationTimer;

    private String strLastQuery;

    public MainPresenter(FragmentActivity newActivity){
        currentActivity = newActivity;
        timeController = new TimerController(this);
        locationAccesser = new LocationAccesser(
                (LocationManager) newActivity.getSystemService(Context.LOCATION_SERVICE)
                , newActivity);
        loadingPanelViewer = new LoadingPanelViewer(newActivity, this);
    }
    public void getMap(String newQuery){
        strLastQuery = newQuery;
        // Android6.0以降なら権限確認.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions();
        } else {
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery);
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(){
        // 権限が許可されていない場合はリクエスト.
        if (currentActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery);
        } else {
            currentActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, R.string.request_permission);
        }
    }
    public void onRequestPermissionsResult(int[] intGrantResults) {
        // 権限リクエストの結果を取得する.
        if (intGrantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // after being allowed permissions, get GoogleMap and start loading CSV.
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery);
        }
    }
    public void loadCsvData(boolean isExistingDataUsed, String newQuery){
        isLoadingCanceled = false;
        locationAccesser.clearMap();
        dataLoader = new ToiletDataLoader(currentActivity, isExistingDataUsed, this, newQuery);
        dataLoader.execute();
    }
    public void setMarkersByFreeWord(String newQuery) {
        isLoadingCanceled = false;
        locationAccesser.clearMap();
        dataSearcher = new ToiletDataSearcher(currentActivity, this, newQuery);
        dataSearcher.execute();
    }
    public void moveCurrentLocation(){
        // GPSをONにした直後は値が取れない場合があるのでTimerで1秒待つ.
        tmrGettingLocationTimer = new Timer();
        tmrGettingLocationTimer.schedule(timeController, 1000L);
    }
    public class TimerController extends TimerTask{
        private final MainPresenter mainPresenter;
        public TimerController(MainPresenter presenter){
            mainPresenter = presenter;
        }
        @Override
        public void run() {
            // Runnable() - run().
            currentActivity.runOnUiThread(
                    () -> {
                        locationAccesser.moveCurrentLocation(mainPresenter);
                    });
        }
    }
    public void startLoadingCsvData(){
        currentActivity.runOnUiThread(
                () -> {
                    // show loading dialog.
                    loadingPanelViewer.show();
                });
    }
    public void stopLoadingCsvData(){
        if(currentActivity == null){
            return;
        }
        // Runnable() - run().
        currentActivity.runOnUiThread(
                () -> {
                    isLoadingCanceled = true;
                    // hide loading dialog.
                    if(loadingPanelViewer != null){
                        loadingPanelViewer.hide();
                    }
                    if(dataLoader != null){
                        dataLoader.stopLoading();
                        dataLoader.cancel(true);
                    }
                });

    }
    public void addMarker(ArrayList<DatabaseAccesser.ToiletInfoModel> toiletInfoModelList){
        currentActivity.runOnUiThread(
                () -> {
                    for (DatabaseAccesser.ToiletInfoModel toiletInfo : toiletInfoModelList) {
                        if(isLoadingCanceled){
                            break;
                        }
                        StringBuilder _newSnippet = new StringBuilder(currentActivity.getString(R.string.marker_address));
                        _newSnippet.append(toiletInfo.address);
                        _newSnippet.append(currentActivity.getString(R.string.marker_availabletime));
                        _newSnippet.append(toiletInfo.availableTime);

                        locationAccesser.addMarker(toiletInfo.toiletName, toiletInfo.latitude, toiletInfo.longitude, _newSnippet.toString());
                    }
                });
    }
    public void showToast(int messageNum){
        // Runnable() - run().
        currentActivity.runOnUiThread(
                () -> {
                    Toast.makeText(currentActivity, messageNum, Toast.LENGTH_SHORT).show();
                });
    }
    public void showErrorDialog(String errorMessage){
        // Runnable() - run().
        currentActivity.runOnUiThread(
                () -> {
                    AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);
                    alert.setTitle(currentActivity.getString(R.string.error_title));
                    alert.setMessage(currentActivity.getString(R.string.error_dialog) + errorMessage);
                    // DialogInterface.OnClickListener() - onClick(DialogInterface dialog, int which).
                    alert.setPositiveButton(currentActivity.getString(android.R.string.ok), null);
                    alert.show();
                });
    }
}

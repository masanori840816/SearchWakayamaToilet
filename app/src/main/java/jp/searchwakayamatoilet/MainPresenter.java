package jp.searchwakayamatoilet;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;

/**
 * Created by masanori on 2016/01/24.
 */
public class MainPresenter {
    //private Activity currentActivity;
    private FragmentActivity currentActivity;
    private LocationAccesser locationAccesser;
    private LoadingPanelViewer loadingPanelViewer;
    private NetworkAccesser networkAccesser;
    private ToiletDataLoader dataLoader;

    public MainPresenter(FragmentActivity newActivity){
        currentActivity = newActivity;
        locationAccesser = new LocationAccesser(
                (LocationManager) newActivity.getSystemService(Context.LOCATION_SERVICE)
                , newActivity);
        loadingPanelViewer = new LoadingPanelViewer(newActivity, this);
        networkAccesser = new NetworkAccesser();
    }
    public void getMap(){
        // Android6.0以降なら権限確認.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions();
        } else {
            locationAccesser.getGoogleMap(currentActivity, this);
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(){
        // 権限が許可されていない場合はリクエスト.
        if (currentActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationAccesser.getGoogleMap(currentActivity, this);
        } else {
            currentActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, R.string.request_permission);
        }
    }
    public void onRequestPermissionsResult(int[] intGrantResults) {
        // 権限リクエストの結果を取得する.
        if (intGrantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // after being allowed permissions, get GoogleMap and start loading CSV.
            locationAccesser.getGoogleMap(currentActivity, this);
        }
    }
    public void loadCsvData(boolean isExistingDataUsed){
        locationAccesser.clearMap();
        dataLoader = new ToiletDataLoader();
        dataLoader.init(currentActivity, isExistingDataUsed, this);
        dataLoader.execute();
    }
    public void setMarkersByFreeWord(String searchWord) {
        locationAccesser.setMarkersByFreeWord(currentActivity, searchWord);
    }
    public void moveCurrentLocation(){
        locationAccesser.moveCurrentLocation();
    }
    public void startLoadingCsvData(){
        currentActivity.runOnUiThread(
                () -> {
                    // show loading dialog.
                    loadingPanelViewer.show();
                });
    }
    public void stopLoadingCsvData(){
        // Runnable() - run().
        currentActivity.runOnUiThread(
                () -> {
                    // hide loading dialog.
                    loadingPanelViewer.hide();
                    dataLoader.stopLoading();
                    dataLoader.cancel(true);
                });

    }
    public void addMarker(String strToiletName, double dblLatitude, double dblLongitude){
        currentActivity.runOnUiThread(
                () -> {
                    locationAccesser.addMarker(strToiletName, dblLatitude, dblLongitude);
                });
    }
    public boolean checkIsNetworkConnected(){
        return networkAccesser.checkIsNetworkConnected(currentActivity);
    }
}

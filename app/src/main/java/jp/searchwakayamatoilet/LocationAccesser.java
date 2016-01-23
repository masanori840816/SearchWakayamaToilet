/**
 * Created by Masanori on 2015/12/12.
 * this class controls location data and map.
 */

package jp.searchwakayamatoilet;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class LocationAccesser  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private static LocationAccesser sLocationAccesser;
    private NetworkAccesser mNetworkAccesser;

    private DatabaseAccesser mDbAccesser;
    private SQLiteDatabase mSqliteDb;
    private DatabaseAccesser.ToiletInfoModel mToiletInfoModel;
    private LoadingPanelViewer mLoadingPanelViewer;

    private boolean mIsDataLoaded;
    private boolean mIsTransactionStarted = false;

    public void initialize(final MainActivity mainActivity, final LocationManager locationManager){
        sLocationAccesser = this;
        mLocationManager = locationManager;

        mNetworkAccesser = new NetworkAccesser();
        mNetworkAccesser.initialize(mainActivity);

        mDbAccesser = new DatabaseAccesser(mainActivity);
        mSqliteDb = mDbAccesser.getWritableDatabase();

        mToiletInfoModel = mDbAccesser.new ToiletInfoModel();

        mLoadingPanelViewer = new LoadingPanelViewer(mainActivity);

        mIsDataLoaded = false;
    }
    public void getGoogleMap(final MainActivity mainActivity, final SupportMapFragment mapFragment){
        // get GoogleMap instance.
        if (mMap != null) {
            return;
        }
        // show map.
        mapFragment.getMapAsync(
                // OnMapReadyCallback - onMapReady(GoogleMap gMap).
                gMap -> {
                    mMap = gMap;
                    mMap.setMyLocationEnabled(true);
                    // 和歌山県庁に移動.
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.22501, 135.1678), 9));
                    // GoogleMap.OnMyLocationButtonClickListener - onMyLocationButtonClick().
                    mMap.setOnMyLocationButtonClickListener(() -> {
                        sLocationAccesser.moveToMyLocation(mainActivity);
                        return false;
                    });
                    sLocationAccesser.loadCsvData(mainActivity);
                });
    }
    public void loadCsvData(final MainActivity mainActivity){
        mMap.clear();

        HandlerThread handlerThread = new HandlerThread("AddMarker");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        // Runnable() - run().
        handler.post(() -> {
            final ArrayList<DatabaseAccesser.ToiletInfoModel> aryToiletInfo = mDbAccesser.search(mSqliteDb);

            if (aryToiletInfo != null
                    && aryToiletInfo.size() > 0) {
                for (DatabaseAccesser.ToiletInfoModel toiletInfo : aryToiletInfo) {

                    // Add marker on UI Thread.
                    Message msgToiletInfo = new Message();
                    msgToiletInfo.what = R.string.handler_get_csv;
                    msgToiletInfo.obj = toiletInfo;
                    executeOnUiThreadHandler.sendMessage(msgToiletInfo);
                }
            } else if (mNetworkAccesser.checkIsNetworkConnected()) {
                executeOnUiThreadHandler.sendEmptyMessage(R.string.handler_show_loading);
                Geocoder geocoder = new Geocoder(mainActivity, Locale.getDefault());
                AssetManager asmAsset = mainActivity.getResources().getAssets();
                try {
                    // CSVの読み込み.
                    InputStream ipsInput = asmAsset.open("toilet-map.csv");
                    InputStreamReader inputStreamReader = new InputStreamReader(ipsInput);
                    BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                    String strLine = "";
                    String[] strSplited;
                    Pattern p = Pattern.compile("^[0-9]+");

                    // Transactionの開始.
                    mSqliteDb.beginTransaction();
                    mIsTransactionStarted = true;

                    // 1行ずつ読み込む.
                    while ((strLine = bufferReader.readLine()) != null) {
                        // とりあえず数値から始まっている行のみ
                        if (p.matcher(strLine).find()) {
                            // とりあえずSplit後に4件以上データがある行のみ.
                            strSplited = strLine.split(",");
                            if (strSplited.length >= 4) {

                                List addressList = geocoder.getFromLocationName(strSplited[3], 1);
                                if (!addressList.isEmpty()) {

                                    Address address = (Address) addressList.get(0);

                                    mToiletInfoModel.toiletName = strSplited[1];
                                    // 都道府県はひとまず和歌山固定.
                                    mToiletInfoModel.district = "和歌山";
                                    mToiletInfoModel.municipality = strSplited[2];
                                    mToiletInfoModel.address = strSplited[3];
                                    mToiletInfoModel.latitude = address.getLatitude();
                                    mToiletInfoModel.longitude = address.getLongitude();
                                    mToiletInfoModel.availableTime = strSplited[4];
                                    String strNearbySightseeing = (strSplited.length > 35) ? strSplited[35] : "";
                                    mToiletInfoModel.nearbySightseeing = strNearbySightseeing;

                                    // Add marker on UI Thread.
                                    Message msgToiletInfo = new Message();
                                    msgToiletInfo.what = R.string.handler_get_csv;
                                    msgToiletInfo.obj = mToiletInfoModel;
                                    executeOnUiThreadHandler.sendMessage(msgToiletInfo);

                                    final String toiletName = strSplited[1];
                                    final String toiletAddress = strSplited[3];
                                    long lngCount = Stream.of(aryToiletInfo)
                                            .filter(toiletInfo -> (toiletInfo.toiletName.equals(toiletName)
                                                    && toiletInfo.address.equals(toiletAddress))).count();

                                    if (lngCount <= 0) {
                                        // if there is no same data, insert as new one.
                                        mDbAccesser.insertInfo(mSqliteDb, mToiletInfoModel);
                                    }
                                }
                            }
                        }
                    }
                    // CommitしてTransactionを終了.
                    mSqliteDb.setTransactionSuccessful();
                    mSqliteDb.endTransaction();

                    mIsTransactionStarted = false;

                    bufferReader.close();
                    ipsInput.close();
                    mIsDataLoaded = true;

                    executeOnUiThreadHandler.sendEmptyMessage(R.string.handler_hide_loading);

                } catch (IOException e) {
                    // TODO: error処理.
                    Log.d("SWT", "Exception:" + e.getLocalizedMessage());
                } catch (SQLiteException e) {
                    // TODO: error処理.
                    Log.d("SWT", "Exception:" + e.getLocalizedMessage());
                }
            }
        });
    }
    public void moveToMyLocation(final FragmentActivity activity){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(3000L);
        locationRequest.setFastestInterval(500L);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(activity.getApplicationContext())
                    .enableAutoManage(activity, this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        // ResultCallback<LocationSettingsResult>() - onResult(LocationSettingsResult settingsResult).
        result.setResultCallback(
            settingsResult -> {
            final Status status = settingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // GPSがOnなら無視.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        // GPSがOffならIntent表示. onActivityResultで結果取得.
                        status.startResolutionForResult(
                                activity, R.string.request_enable_location);
                    } catch (IntentSender.SendIntentException e) {
                        // TODO; 例外処理.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Locationが無効なら無視.
                    break;
            }
        });
    }
    public void addMarker(String strToiletName, double dblLatitude, double dblLongitude){
        if (mMap != null) {
            // 表示したマップにマーカーを追加する.
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(dblLatitude, dblLongitude)).title(strToiletName));
        }
    }
    public void onPause(Activity activeActivity){

        if(mIsTransactionStarted
                && ! mIsDataLoaded){
            // if inserting datas hasn't finished, end transaction.
            mSqliteDb.setTransactionSuccessful();
            mSqliteDb.endTransaction();
            mIsTransactionStarted = false;
        }

        // ネットワーク状態の監視をストップ.
        activeActivity.unregisterReceiver(mNetworkAccesser);
    }
    public void onResume(Activity activeActivity) {
        // ネットワーク状態の監視を再開.
        activeActivity.registerReceiver(mNetworkAccesser, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
    }
    @Override
    public void onConnectionFailed(ConnectionResult result){
        Log.d("SWT", "Failed");
    }
    @Override
    public void onConnectionSuspended(int cause){
        Log.d("SWT", "Suspended");
    }
    @Override
    public void onConnected(Bundle bundle){
        Log.d("SWT", "Connected");
    }
    public void moveCurrentLocation(){
        try {
            // 現在位置を中央に表示.
            Location currentLocation = mMap.getMyLocation();

            if(currentLocation == null){
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                // 位置情報が取得できるプロバイダから現在位置の取得.
                currentLocation = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(criteria, true));
            }
            if(currentLocation == null) {
                // if can't get location, show Toast.
                MainActivity.showToastFailedGettingLocation();
            }
            else{
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13));
            }
        }catch(SecurityException ex){
            Log.d("SWT Error", ex.getLocalizedMessage());
        }
    }
    public void setMarkersByFreeWord(final String strWord){
        mMap.clear();

        HandlerThread handlerThread = new HandlerThread("AddMarker");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        // Runnable() - run().
        handler.post(() -> {
            mDbAccesser.setSearchCriteriaFromFreeWord(strWord);
            ArrayList<DatabaseAccesser.ToiletInfoModel> aryToiletInfo = mDbAccesser.search(mSqliteDb);

            if (aryToiletInfo != null) {
                for (DatabaseAccesser.ToiletInfoModel toiletInfo : aryToiletInfo) {
                    // Add marker on UI Thread.
                    Message msgToiletInfo = new Message();
                    msgToiletInfo.what = R.string.handler_get_csv;
                    msgToiletInfo.obj = toiletInfo;
                    executeOnUiThreadHandler.sendMessage(msgToiletInfo);
                }
            }
        });
    }
    private Handler executeOnUiThreadHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case R.string.handler_get_csv:
                    if(msg.obj != null){
                        DatabaseAccesser.ToiletInfoModel toiletInfo = (DatabaseAccesser.ToiletInfoModel)msg.obj;
                        addMarker(toiletInfo.toiletName, toiletInfo.latitude, toiletInfo.longitude);
                    }
                    break;
                case R.string.handler_show_loading:
                    mLoadingPanelViewer.show();
                    break;
                case R.string.handler_hide_loading:
                    mLoadingPanelViewer.close();
                    break;
            }
        }
    };
}

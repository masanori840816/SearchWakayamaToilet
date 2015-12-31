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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
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

    public void initialize(final MainActivity mainActivity, final LocationManager locationManager){
        sLocationAccesser = this;
        mLocationManager = locationManager;

        mNetworkAccesser = new NetworkAccesser();
        mNetworkAccesser.initialize(mainActivity);

        mDbAccesser = new DatabaseAccesser(mainActivity);
        mSqliteDb = mDbAccesser.getWritableDatabase();

        mToiletInfoModel = new DatabaseAccesser(mainActivity).new ToiletInfoModel();
    }
    public void getGoogleMap(final MainActivity mainActivity, final SupportMapFragment mapFragment){
        // get GoogleMap instance.
        if (mMap != null) {
            return;
        }
        // show map.
        mapFragment.getMapAsync(
                new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap gMap) {
                        mMap = gMap;
                        mMap.setMyLocationEnabled(true);
                        // 和歌山県庁に移動.
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.22501, 135.1678), 9));
                        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                sLocationAccesser.moveToMyLocation(mainActivity);
                                return false;
                            }
                        });
                        sLocationAccesser.loadCsvData(mainActivity);
                    }
                });
    }
    public void loadCsvData(final MainActivity mainActivity){
        HandlerThread handlerThread = new HandlerThread("AddMarker");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                ArrayList<DatabaseAccesser.ToiletInfoModel> aryToiletInfo = mDbAccesser.search(mSqliteDb);

                if (aryToiletInfo != null
                        && aryToiletInfo.size() > 0) {
                    for (DatabaseAccesser.ToiletInfoModel toiletInfo : aryToiletInfo) {
                        Log.d("SWT", "ID:" + toiletInfo.id);
                        Log.d("SWT", "Name:" + toiletInfo.toiletName);
                    }
                } else if (mNetworkAccesser.checkIsNetworkConnected()) {
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

                        // 1行ずつ読み込む.
                        while ((strLine = bufferReader.readLine()) != null) {
                            // とりあえず数値から始まっている行のみ
                            if (p.matcher(strLine).find()) {
                                // とりあえずSplit後に4件以上データがある行のみ.
                                strSplited = strLine.split(",");
                                if (strSplited.length >= 4) {
                                    // とりあえず名称と住所のみ.
                                    List addressList = geocoder.getFromLocationName(strSplited[3], 1);
                                    if (addressList.isEmpty()) {
                                        Log.d("swtSearch", "list is empty");
                                    } else {
                                        Address address = (Address) addressList.get(0);
                                        // UIスレッドで取得したデータを受け取れるようにする.
                                        MainActivity.setNewMarker(strSplited[1], address.getLatitude(), address.getLongitude());

                                        mSqliteDb.beginTransaction();
                                        // Insert.
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

                                        mDbAccesser.insertInfo(mSqliteDb, mToiletInfoModel);

                                        mSqliteDb.setTransactionSuccessful();

                                        mSqliteDb.endTransaction();
                                    }
                                }
                            }
                        }

                        bufferReader.close();
                        ipsInput.close();

                    } catch (IOException e) {
                        // TODO: error処理.
                        Log.d("SWT", "Exception:" + e.getLocalizedMessage());
                    } catch (SQLiteException e) {
                        // TODO: error処理.
                        Log.d("SWT", "Exception:" + e.getLocalizedMessage());
                    } finally {
 //                       mSqliteDb.endTransaction();
                    }
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
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
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
            }
        });
    }
    public void adMarker(String strToiletName, double dblLatitude, double dblLongitude){
        if (mMap != null) {
            // 表示したマップにマーカーを追加する.
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(dblLatitude, dblLongitude)).title(strToiletName));
        }
    }
    public void onPause(Activity activeActivity){
        // ネットワーク状態の監視をストップ.
        activeActivity.unregisterReceiver(mNetworkAccesser);
    }
    public void onResume(Activity activeActivity) {
        // ネットワーク状態の監視を再開.
        activeActivity.registerReceiver(mNetworkAccesser, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
    }
    public void onDestroy(){
        //mRealm.close();
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
}

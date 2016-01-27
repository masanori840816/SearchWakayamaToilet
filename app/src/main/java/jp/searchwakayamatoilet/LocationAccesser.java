/**
 * Created by Masanori on 2015/12/12.
 * this class controls location data and map.
 */

package jp.searchwakayamatoilet;

import android.app.Activity;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

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

import java.util.ArrayList;

public class LocationAccesser  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap map;
    private LocationManager mLocationManager;
    private GoogleApiClient apiClient;
    private LocationAccesser locationAccesser;


    private DatabaseAccesser mDbAccesser;
    private SQLiteDatabase mSqliteDb;

    private HandlerThread mHandlerThread;
    private boolean mIsDataLoading;

    private Activity currentActivity;


    public LocationAccesser(final LocationManager locationManager, Activity newActivity){
        locationAccesser = this;
        mLocationManager = locationManager;

        mDbAccesser = new DatabaseAccesser(newActivity);
        mSqliteDb = mDbAccesser.getWritableDatabase();

        mIsDataLoading = false;

        mHandlerThread = new HandlerThread("AddMarker");

        currentActivity = newActivity;
    }
    public void getGoogleMap(final FragmentActivity fragmentActivity, MainPresenter presenter){
        // get GoogleMap instance.
        if (map != null) {
            return;
        }
        // show map.
        ((SupportMapFragment) fragmentActivity.getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(
                        // OnMapReadyCallback - onMapReady(GoogleMap gMap).
                        gMap -> {
                            map = gMap;
                            map.setMyLocationEnabled(true);
                            // 和歌山県庁に移動.
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.22501, 135.1678), 9));
                            // GoogleMap.OnMyLocationButtonClickListener - onMyLocationButtonClick().
                            map.setOnMyLocationButtonClickListener(() -> {
                                locationAccesser.moveToMyLocation(fragmentActivity);
                                return false;
                            });
                            presenter.loadCsvData(true);
                        });
    }
    public void clearMap(){
        map.clear();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("SWT", "Failed");
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("SWT", "Suspended");
    }
    @Override
    public void onConnected(Bundle bundle){
        Log.d("SWT", "Connected");
    }

    public void setMarkersByFreeWord(final Activity activity, final String strWord){
        map.clear();

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
                    // Runnable() - run().
                    activity.runOnUiThread(
                            () -> {
                                this.addMarker(toiletInfo.toiletName, toiletInfo.latitude, toiletInfo.longitude);
                            });
                }
            }
        });
    }
    public void moveCurrentLocation(){
        try {
            // 現在位置を中央に表示.
            Location currentLocation = map.getMyLocation();

            if(currentLocation == null){
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                // 位置情報が取得できるプロバイダから現在位置の取得.
                currentLocation = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(criteria, true));
            }
            if(currentLocation == null) {
                // if can't get location, show Toast.
                Toast.makeText(currentActivity, R.string.toast_failed_getting_location, Toast.LENGTH_SHORT).show();
            }
            else{
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13));
            }
        }catch(SecurityException ex){
            Log.d("SWT Error", ex.getLocalizedMessage());
        }
    }
    public void addMarker(String strToiletName, double dblLatitude, double dblLongitude){
        if (map != null) {
            // 表示したマップにマーカーを追加する.
            map.addMarker(new MarkerOptions().position(
                    new LatLng(dblLatitude, dblLongitude)).title(strToiletName));
        }
    }
    private void moveToMyLocation(final FragmentActivity activity){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(3000L);
        locationRequest.setFastestInterval(500L);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        if(apiClient == null) {
            apiClient = new GoogleApiClient
                    .Builder(activity.getApplicationContext())
                    .enableAutoManage(activity, this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build());
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
}

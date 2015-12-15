package jp.searchwakayamatoilet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.location.Address;
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
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by Masanori on 2015/12/12.
 */
public class LocationAccesser  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private static LocationAccesser sLocationAccesser;

    public void initialize(final LocationManager locationManager){
        sLocationAccesser = new LocationAccesser();
        mLocationManager = locationManager;
    }
    public void loadCsvData(final MainActivity mainActivity){
        HandlerThread handlerThread = new HandlerThread("AddMarker");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
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
                                }
                            }
                        }
                    }
                    bufferReader.close();
                    ipsInput.close();

                } catch (IOException e) {
                    Log.d("swtSearch", "Exception 発生");
                }
            }
        });
    }
    public void moveToMyLocation(final FragmentActivity activity){
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(activity.getApplicationContext())
                    .enableAutoManage(activity, 34992, this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500L);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // GPSがOnなら現在位置を中央に表示.
                        sLocationAccesser.moveCurrentLocation();
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
    @Override
    public void onConnectionFailed(ConnectionResult result){

    }
    @Override
    public void onConnectionSuspended(int cause){
        Log.d("SWT", "Suspended");
    }
    @Override
    public void onConnected(Bundle bundle){
        Log.d("SWT", "Connected");
    }
    @Override
    public void onProviderEnabled(String strProvider){

    }
    @Override
    public void onProviderDisabled(String strProvider){

    }
    @Override
    public void onLocationChanged(Location lctCurrentLocation){
        moveCurrentLocation();
    }
    @Override
    public void onStatusChanged(String strProvider, int status, Bundle extras){

    }
    public void moveCurrentLocation(){
        try {
            // 現在位置を中央に表示.
// TODO: ぬるぽ.
            //Location currentLocation = mLocationManager.getLastKnownLocation("gps");
            //if(currentLocation != null){
         //       mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
            //}
        }catch(SecurityException ex){
            Log.d("SWT Error", ex.getLocalizedMessage());
        }
    }

}

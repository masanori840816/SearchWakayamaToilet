package jp.searchwakayamatoilet;

/**
 * Created by Masanori on 2015/12/09.
 */
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private GoogleMap mMap;

    private String mStrToiletName;
    private double mDblLatitude;
    private double mDblLongitude;
    private LocationManager mLocationManager;
    private final int REQUEST_PERMISSIONS = 1;
    private final int REQUEST_ENABLE_LOCATION = 2;
    private static MainActivity mMainActivity;

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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainActivity = this;
        NetworkAccesser.initialize(mMainActivity);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Android6.0以降なら権限確認.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions();
        } else {
            this.getNewMap();
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(){
        // 権限が許可されていない場合はリクエスト.
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.getNewMap();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int intRequestCode, String[] strPermissions, int[] intGrantResults) {
        // 権限リクエストの結果を取得する.
        if (intRequestCode == REQUEST_PERMISSIONS) {
            if (intGrantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // after being allowed permissions, get GoogleMap and start loading CSV.
                this.getNewMap();
            }
        }else {
            super.onRequestPermissionsResult(intRequestCode, strPermissions, intGrantResults);
        }
    }
    private void getNewMap(){
        // get GoogleMap instance.
        if (mMap != null) {
            return;
        }
        // マップの表示.
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(
                        new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap gMap) {
                                mMap = gMap;
                                mMap.setMyLocationEnabled(true);
                                // 和歌山県庁に移動.
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.22501, 135.1678), 9));
                                mMainActivity.loadCsv();
                            }
                        });
    }
    public void loadCsv(){

        if(! this.checkIsNetworkConnected()){
            return;
        }

        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 34992, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.checkIsGpsEnable(mGoogleApiClient, mMainActivity);

        HandlerThread handlerThread = new HandlerThread("AddMarker");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(mMainActivity, Locale.getDefault());
                AssetManager asmAsset = mMainActivity.getResources().getAssets();
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
                                    mMainActivity.mStrToiletName = strSplited[1];
                                    mMainActivity.mDblLatitude = address.getLatitude();
                                    mMainActivity.mDblLongitude = address.getLongitude();
                                    // UIスレッドでマーカー設置.
                                    getCsvHandler.sendEmptyMessage(1);
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
    private static void checkIsGpsEnable(GoogleApiClient googleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500L);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // GPSがOnなら現在位置を中央に表示.
                        mMainActivity.moveCurrentLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // GPSがOffならIntent表示. onActivityResultで結果取得.
                            status.startResolutionForResult(
                                    activity, mMainActivity.REQUEST_ENABLE_LOCATION);
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
    public void moveCurrentLocation(){
        try {
            // 現在位置を中央に表示.

            Location currentLocation = mLocationManager.getLastKnownLocation("gps");
            if(currentLocation != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
            }
        }catch(SecurityException ex){
            Log.d("SWT Error", ex.getLocalizedMessage());
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE_LOCATION:
                if(resultCode == RESULT_OK){
                    try {
                        // Locationが有効なら.
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 500, this);
                    }catch (SecurityException e){
                        // TODO: 例外処理.
                    }
                }
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private Handler getCsvHandler = new Handler() {
        public void handleMessage(Message msg) {
            addMarker();
        }
    };
    private void addMarker() {
        if (mMap != null) {
            // 表示したマップにマーカーを追加する.
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(mMainActivity.mDblLatitude, mMainActivity.mDblLongitude)).title(mMainActivity.mStrToiletName));

        }
    }
    private boolean checkIsNetworkConnected(){
        NetworkInfo networkInfo = ((ConnectivityManager) mMainActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        return false;
    }
}

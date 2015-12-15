package jp.searchwakayamatoilet;

/**
 * Created by Masanori on 2015/12/09.
 */
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;

public class MainActivity extends FragmentActivity{
    private GoogleMap mMap;

    private String mStrToiletName;
    private double mDblLatitude;
    private double mDblLongitude;
    private final int REQUEST_PERMISSIONS = 1;
    private static MainActivity sMainActivity;
    private LocationAccesser mLocationAccesser;
    private NetworkAccesser mNetworkAccesser;

    public static void networkStatusChanged(){
        sMainActivity.loadToiletDatas();
    }
    public static void setNewMarker(String newToiletName, double newLatitude, double newLongitude){
        // UIスレッドで取得したデータを受け取れるようにする.
        sMainActivity.mStrToiletName = newToiletName;
        sMainActivity.mDblLatitude = newLatitude;
        sMainActivity.mDblLongitude = newLongitude;
        // UIスレッドでマーカー設置.
        sMainActivity.getCsvHandler.sendEmptyMessage(1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sMainActivity = this;
        mLocationAccesser = new LocationAccesser();
        mLocationAccesser.initialize((LocationManager) getSystemService(Context.LOCATION_SERVICE));

        mNetworkAccesser = new NetworkAccesser();

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
                                mMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
                                    @Override
                                    public boolean onMyLocationButtonClick() {
                                        mLocationAccesser.moveToMyLocation(sMainActivity);
                                        return false;
                                    }
                                });
                                // CSVデータを読み込んでマーカーを設置.
                                loadToiletDatas();
                            }
                        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case R.string.request_enable_location:
                if(resultCode == RESULT_OK){
                    try {
                        // Locationが有効なら.
                        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 500, this);
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
    @Override
    public void onPause(){
        // ネットワーク状態の監視をストップ.
        unregisterReceiver(mNetworkAccesser);
        super.onPause();
    }
    @Override
    public void onResume(){
        // ネットワーク状態の監視を再開.
        registerReceiver(mNetworkAccesser, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
        super.onResume();
    }
    @Override
    public void onStop(){
        // ネットワーク状態の監視をストップ.
        unregisterReceiver(mNetworkAccesser);
        super.onStop();
    }
    private Handler getCsvHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mMap != null) {
                // 表示したマップにマーカーを追加する.
                mMap.addMarker(new MarkerOptions().position(
                        new LatLng(sMainActivity.mDblLatitude, sMainActivity.mDblLongitude)).title(sMainActivity.mStrToiletName));

            }
        }
    };
    private void loadToiletDatas(){
        if(mNetworkAccesser.checkIsNetworkConnected((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))){
            // ネットワークにつながっていればCSVから住所を読み込んでマーカー設置.
            mLocationAccesser.loadCsvData(this);
        }
        else{
            // ネットワークにつながっていなければToast表示.
            Toast.makeText(this, R.string.toast_no_networks, Toast.LENGTH_SHORT).show();
        }
    }
}

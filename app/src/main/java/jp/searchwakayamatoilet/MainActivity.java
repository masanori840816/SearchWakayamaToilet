/**
 * Created by Masanori on 2015/12/09.
 * this class is main controller of this application.
 */
package jp.searchwakayamatoilet;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity{


    private final int REQUEST_PERMISSIONS = 1;
    private static MainActivity sMainActivity;
    private LocationAccesser mLocationAccesser;

    private TimerController mTimeController;
    private Timer mTmrGettingLocationTimer;

    public void networkStatusChanged(){
        // TODO: アプリを使用中＋ネットワーク接続時のみ実行するよう修正.
        //mLocationAccesser.loadCsvData(this);
    }
    public static void showToastFailedGettingLocation(){
        Toast.makeText(sMainActivity, R.string.toast_failed_getting_location, Toast.LENGTH_SHORT).show();
    }
    public ConnectivityManager getConnectivityManager(){
        return (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    public void showToastNoNetworks(){
        // ネットワークにつながっていなければToast表示.
        Toast.makeText(this, R.string.toast_no_networks, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sMainActivity = this;
        mLocationAccesser = new LocationAccesser();
        mLocationAccesser.initialize(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));

        mTimeController = new TimerController();

        Toolbar _toolbar = (Toolbar) findViewById(R.id.toolbar);
        _toolbar.inflateMenu(R.menu.menu_main);

        SearchView _searchView = (SearchView) MenuItemCompat.getActionView(_toolbar.getMenu().findItem(R.id.searchview));
        // hide Submit button.
        _searchView.setSubmitButtonEnabled(false);

        _searchView.setQueryHint(getString(R.string.searchview_queryhint));

        _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                // search toilet name or address by input words by Submit button or EnterKey.
                mLocationAccesser.setMarkersByFreeWord(searchWord);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 入力される度に呼び出される
                return false;
            }
        });

        // Android6.0以降なら権限確認.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions();
        } else {
            mLocationAccesser.getGoogleMap(this, (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(){
        // 権限が許可されていない場合はリクエスト.
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationAccesser.getGoogleMap(this, (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
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
                mLocationAccesser.getGoogleMap(this, (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            }
        }else {
            super.onRequestPermissionsResult(intRequestCode, strPermissions, intGrantResults);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case R.string.request_enable_location:
                if(resultCode == RESULT_OK){
                    // GPSをONにした直後は値が取れない場合があるのでTimerで1秒待つ.
                    mTmrGettingLocationTimer = new Timer();
                    mTmrGettingLocationTimer.schedule(mTimeController, 1000L);
                }
                break;
        }
    }

    @Override
    public void onPause(){
        // ネットワーク状態の監視をストップ.
        mLocationAccesser.onPause(this);
        super.onPause();
    }
    @Override
    public void onResume(){
        // ネットワーク状態の監視を再開.
        mLocationAccesser.onResume(this);
        super.onResume();
    }
    private Handler executeOnUiThreadHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case R.string.handler_get_location:
                    // Locationが有効なら現在位置を取得.
                    mLocationAccesser.moveCurrentLocation();
                    break;
            }

        }
    };
    public class TimerController extends TimerTask{
        @Override
        public void run() {
            // get location data.
            executeOnUiThreadHandler.sendEmptyMessage(R.string.handler_get_location);
        }
    }
}

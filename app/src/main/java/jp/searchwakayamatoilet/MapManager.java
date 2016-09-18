package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/09/04.
 * this manages map and location data.
 */
import android.app.Activity;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
public class MapManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private GoogleMap map;
    private GoogleApiClient apiClient;
    private final static int RequestGpsEnable = 2;
    @NonNull
    private final LocationManager CurrentLocationManager;
    @NonNull
    private final Activity CurrentActivity;
    private final MapManager CurrentMapManager;

    public MapManager(@NonNull LocationManager locationManager, @NonNull Activity activity){
        CurrentLocationManager = locationManager;
        CurrentActivity = activity;
        CurrentMapManager = this;
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
    }
    @Override
    public void onConnectionSuspended(int cause) {
    }
    @Override
    public void onConnected(Bundle bundle) {
    }
    public int getRequestGpsEnable(){
        return RequestGpsEnable;
    }
    public void setGoogleMap(@NonNull FragmentActivity fragmentActivity, MainPresenter presenter, String newQuery) {
        // get GoogleMap instance.
        if (map != null) {
            return;
        }
        // show map.
        ((SupportMapFragment)fragmentActivity.getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(gMap ->
        {
            try {
                map = gMap;
                map.setMyLocationEnabled(true);
                map.setInfoWindowAdapter(new ToiletInfoWindowViewer(fragmentActivity));
                // 和歌山県庁に移動.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.22501, 135.1678), 9f));
                map.setOnMyLocationButtonClickListener(() ->{
                    CurrentMapManager.moveToMyLocation(fragmentActivity, presenter);
                    return false;
                });
                presenter.loadToiletInfo(newQuery);
            } catch (SecurityException e){
                presenter.showErrorDialog(e.getMessage());
            }
        });
    }
    public void clearMap() {
        if(map == null){
            return;
        }
        map.clear();
    }
    public void moveCurrentLocation(MainPresenter presenter) {
        try {
            // 現在位置を中央に表示.
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            // 位置情報が取得できるプロバイダから現在位置の取得.
            Location currentLocation = CurrentLocationManager.getLastKnownLocation(CurrentLocationManager.getBestProvider(criteria, true));
            if (currentLocation == null) {
                // if can't get location, show Toast.
                Toast.makeText(CurrentActivity, R.string.toast_failed_getting_location, Toast.LENGTH_SHORT).show();
            } else {
                if(map != null){
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13f));
                }
            }
        } catch (SecurityException e) {
            presenter.showErrorDialog(e.getMessage());
        }

    }
    public void addMarker(@NonNull String strToiletName, @NonNull double dblLatitude, @NonNull double dblLongitude, @NonNull String strSnippet) {
        if(map == null){
            return;
        }
        // 表示したマップにマーカーを追加する.
        map.addMarker(new MarkerOptions().position(
                new LatLng(dblLatitude, dblLongitude)).title(strToiletName).snippet(strSnippet).icon(BitmapDescriptorFactory.fromResource(R.mipmap.swt_marker)));
    }
    private void moveToMyLocation(FragmentActivity activity, MainPresenter presenter) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(3000L);
        locationRequest.setFastestInterval(500L);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        if(apiClient == null){
            apiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                    .enableAutoManage(activity, this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build());
        result.setResultCallback (settingsResult -> {
            Status status = settingsResult.getStatus();
            switch (status.getStatusCode()){
                case LocationSettingsStatusCodes.SUCCESS:
                    // GPSがOnなら何もしない.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        // GPSがOffならIntent表示. onActivityResultで結果取得.
                        status.startResolutionForResult(activity, RequestGpsEnable);
                    } catch (IntentSender.SendIntentException e) {
                        presenter.showErrorDialog(e.getMessage());
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // GSPが使用不可なら何もしない.
                    break;
            }
        });
    }
}

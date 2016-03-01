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

public class LocationAccesser  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap map;
    private final LocationManager locationManager;
    private GoogleApiClient apiClient;
    private final LocationAccesser locationAccesser;

    private final DatabaseAccesser dbAccesser;
    private final SQLiteDatabase sqlite;

    private final Activity currentActivity;

    public LocationAccesser(final LocationManager newLocationManager, Activity newActivity){
        locationAccesser = this;
        locationManager = newLocationManager;

        dbAccesser = new DatabaseAccesser(newActivity);
        sqlite = dbAccesser.getWritableDatabase();
        currentActivity = newActivity;
    }
    public void getGoogleMap(final FragmentActivity fragmentActivity, MainPresenter presenter, String newQuery){
        // get GoogleMap instance.
        if (map != null) {
            return;
        }
        // show map.
        ((SupportMapFragment) fragmentActivity.getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(
                        // OnMapReadyCallback - onMapReady(GoogleMap gMap).
                        gMap -> {
                            try {
                                map = gMap;
                                map.setMyLocationEnabled(true);
                                map.setInfoWindowAdapter(new ToiletInfoWindowViewer(fragmentActivity));
                                // 和歌山県庁に移動.
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.22501, 135.1678), 9));
                                // GoogleMap.OnMyLocationButtonClickListener - onMyLocationButtonClick().
                                map.setOnMyLocationButtonClickListener(() -> {
                                    locationAccesser.moveToMyLocation(fragmentActivity, presenter);
                                    return false;
                                });
                                presenter.loadCsvData(true, newQuery);
                            } catch (SecurityException ex) {
                                presenter.showErrorDialog(ex.getLocalizedMessage());
                            }
                        });
    }
    public void clearMap(){
        map.clear();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }
    @Override
    public void onConnectionSuspended(int cause) {
    }
    @Override
    public void onConnected(Bundle bundle){
    }
    public void moveCurrentLocation(MainPresenter presenter){
        try {
            // 現在位置を中央に表示.
            Location currentLocation = map.getMyLocation();

            if(currentLocation == null){
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                // 位置情報が取得できるプロバイダから現在位置の取得.
                currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
            }
            if(currentLocation == null) {
                // if can't get location, show Toast.
                Toast.makeText(currentActivity, R.string.toast_failed_getting_location, Toast.LENGTH_SHORT).show();
            }
            else{
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13));
            }
        }catch(SecurityException ex){
            presenter.showErrorDialog(ex.getLocalizedMessage());
        }
    }
    public void addMarker(String strToiletName, double dblLatitude, double dblLongitude, String strSnippet){
        if (map != null) {
            // 表示したマップにマーカーを追加する.
            map.addMarker(new MarkerOptions().position(
                    new LatLng(dblLatitude, dblLongitude))
                    .title(strToiletName)
                    .snippet(strSnippet)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.swt_marker)));
        }
    }
    private void moveToMyLocation(final FragmentActivity activity, MainPresenter presenter){
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
                            } catch (IntentSender.SendIntentException ex) {
                                presenter.showErrorDialog(ex.getLocalizedMessage());
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Locationが無効なら無視.
                            break;
                    }
                });
    }
}

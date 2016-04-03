/**
 * Created by Masanori on 2015/12/12.
 * this class controls location data and map.
 */

package jp.searchwakayamatoilet

import android.app.Activity
import android.content.IntentSender
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LocationAccesser(private val locationManager: LocationManager, private val currentActivity: Activity) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var map: GoogleMap? = null
    private var apiClient: GoogleApiClient? = null
    private val locationAccesser: LocationAccesser

    init {
        locationAccesser = this
    }

    fun getGoogleMap(fragmentActivity: FragmentActivity?, presenter: MainPresenter?, newQuery: String?) {
        // get GoogleMap instance.
        if (map != null) {
            return
        }
        // show map.
        (fragmentActivity?.supportFragmentManager?.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { gMap ->
            try {
                map = gMap
                map!!.isMyLocationEnabled = true
                map!!.setInfoWindowAdapter(ToiletInfoWindowViewer(fragmentActivity))
                // 和歌山県庁に移動.
                map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(34.22501, 135.1678), 9f))
                // GoogleMap.OnMyLocationButtonClickListener - onMyLocationButtonClick().
                map!!.setOnMyLocationButtonClickListener {
                    locationAccesser.moveToMyLocation(fragmentActivity, presenter)
                    false
                }
                presenter?.loadCsvData(true, newQuery)
            } catch (ex: SecurityException) {
                presenter?.showErrorDialog(ex.message)
            }
        }
    }

    fun clearMap() {
        map!!.clear()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
    }

    override fun onConnectionSuspended(cause: Int) {
    }

    override fun onConnected(bundle: Bundle?) {
    }

    fun moveCurrentLocation(presenter: MainPresenter?) {
        try {
            // 現在位置を中央に表示.
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_COARSE
            // 位置情報が取得できるプロバイダから現在位置の取得.
            val currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true))
            if (currentLocation == null) {
                // if can't get location, show Toast.
                Toast.makeText(currentActivity, R.string.toast_failed_getting_location, Toast.LENGTH_SHORT).show()
            } else {
                map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude, currentLocation.longitude), 13f))
            }
        } catch (ex: SecurityException) {
            presenter?.showErrorDialog(ex.message)
        }

    }

    fun addMarker(strToiletName: String, dblLatitude: Double, dblLongitude: Double, strSnippet: String) {
        if (map != null) {
            // 表示したマップにマーカーを追加する.
            map!!.addMarker(MarkerOptions().position(
                    LatLng(dblLatitude, dblLongitude)).title(strToiletName).snippet(strSnippet).icon(BitmapDescriptorFactory.fromResource(R.mipmap.swt_marker)))
        }
    }

    private fun moveToMyLocation(activity: FragmentActivity?, presenter: MainPresenter?) {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 3000L
        locationRequest.fastestInterval = 500L
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        if (apiClient == null
            && activity != null) {
            apiClient = GoogleApiClient.Builder(activity.applicationContext).enableAutoManage(activity, this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build()
        }

        val result = LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build())
        // ResultCallback<LocationSettingsResult>() - onResult(LocationSettingsResult settingsResult).
        result.setResultCallback { settingsResult ->
            val status = settingsResult.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    // GPSがOffならIntent表示. onActivityResultで結果取得.
                    status.startResolutionForResult(
                            activity, R.string.request_enable_location)
                } catch (ex: IntentSender.SendIntentException) {
                    presenter?.showErrorDialog(ex.message)
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }// GPSがOnなら無視.
            // Locationが無効なら無視.
        }
    }
}

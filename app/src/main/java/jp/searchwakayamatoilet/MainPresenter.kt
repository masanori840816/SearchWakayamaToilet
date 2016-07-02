package jp.searchwakayamatoilet

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.support.v4.view.MenuItemCompat
import android.widget.SearchView

import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

import java.util.ArrayList

import java.util.Timer
import java.util.TimerTask

/**
 * Created by masanori on 2016/01/24.
 * this class for controlling MainPage.
 */
class MainPresenter(private val currentActivity: FragmentActivity, lastQuery: String) {
    private val locationAccesser: LocationAccesser
    private val loadingPanelViewer: LoadingPanelViewer
    private var dataLoader: ToiletDataLoader? = null
    private var isLoadingCanceled: Boolean = false

    private val timeController: TimerController
    private var compositeSubscription: CompositeSubscription? = null

    private var suggestList: ListView? = null
    var strLastQuery: String

    init {

        timeController = TimerController(this)
        locationAccesser = LocationAccesser(
                currentActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager, currentActivity as Activity)
        loadingPanelViewer = LoadingPanelViewer(currentActivity)

        strLastQuery = lastQuery
        init()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissions() {
        // 権限が許可されていない場合はリクエスト.
        if (currentActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery)
        } else {
            currentActivity.requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), R.string.request_permission)
        }
    }

    fun onRequestPermissionsResult(intGrantResults: IntArray) {
        // 権限リクエストの結果を取得する.
        if (intGrantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // after being allowed permissions, get GoogleMap and start loading CSV.
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery)
        }
    }

    fun loadCsvData(isExistingDataUsed: Boolean, newQuery: String?) {
        isLoadingCanceled = false
        locationAccesser.clearMap()

        dataLoader = ToiletDataLoader(currentActivity, isExistingDataUsed, this, newQuery)
        dataLoader?.execute()
    }

    private fun moveCurrentLocation() {
        // GPSをONにした直後は値が取れない場合があるのでTimerで1秒待つ.
        val tmrGettingLocationTimer = Timer()
        tmrGettingLocationTimer.schedule(timeController, 1000L)
    }

    inner class TimerController(private val mainPresenter: MainPresenter) : TimerTask() {
        override fun run() {
            currentActivity.runOnUiThread { locationAccesser.moveCurrentLocation(mainPresenter) }
        }
    }

    fun startLoadingCsvData() {
        currentActivity.runOnUiThread { // show loading dialog.
            loadingPanelViewer.show()
        }
    }

    fun addMarker(toiletInfoModelList: ArrayList<DatabaseAccesser.ToiletInfoModel>) {
        currentActivity.runOnUiThread {
            for (toiletInfo in toiletInfoModelList) {
                if (isLoadingCanceled) {
                    break
                }
                var newSnippet = currentActivity.getString(R.string.marker_address)
                newSnippet += toiletInfo.address
                newSnippet += currentActivity.getString(R.string.marker_availabletime)
                newSnippet += toiletInfo.availableTime

                locationAccesser.addMarker(toiletInfo.toiletName, toiletInfo.latitude, toiletInfo.longitude, newSnippet)
            }
        }
    }

    fun showToast(messageNum: Int) {
        // Runnable() - run().
        currentActivity.runOnUiThread {
            Toast.makeText(currentActivity, messageNum, Toast.LENGTH_SHORT)
                    .show()
        }
    }

    fun showErrorDialog(errorMessage: String?) {
        currentActivity.runOnUiThread {
            val alert = AlertDialog.Builder(currentActivity)
            alert.setTitle(currentActivity.getString(R.string.error_title))
            alert.setMessage(currentActivity.getString(R.string.error_dialog) + errorMessage)
            alert.setPositiveButton(currentActivity.getString(android.R.string.ok), null)
            alert.show()
        }
    }
    fun onResume(){
        val subscription: Subscription = RxBusProvider.getInstance()
                .toObserverable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it is LoadingPanelEvent){
                        stopLoadingCsvData()
                    }
                }
        compositeSubscription = CompositeSubscription(subscription)
    }
    fun onPaused() {
        compositeSubscription?.unsubscribe()
    }
    fun onActivityResult(requestCode: Int, resultCode: Int){
        when (requestCode) {
            locationAccesser.getRequestGpsEnable() ->
                if (resultCode == Activity.RESULT_OK) {
                    // get location data.
                    moveCurrentLocation()
                }
        }
    }

    private fun init() {
        val toolbar = currentActivity.findViewById(R.id.toolbar) as Toolbar
        toolbar.inflateMenu(R.menu.menu_main)

        val searchManager = currentActivity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = toolbar.menu.findItem(R.id.searchview)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(currentActivity.componentName))

        MenuItemCompat.setActionView(searchItem, searchView)

        // hide Submit button.
        searchView.isSubmitButtonEnabled = false

        searchView.queryHint = currentActivity.getString(R.string.searchview_queryhint)
        searchView.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        searchView.setIconifiedByDefault(true)

        searchView.setOnCloseListener {
            suggestList?.visibility = View.INVISIBLE
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // hide suggest items.
                suggestList?.visibility = View.INVISIBLE
                // search toilet name or address by input words by Submit button or EnterKey.
                setMarkersByFreeWord(query)
                strLastQuery = query
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // if the textarea empty, show suggest items.
                if (newText.isEmpty()) {
                    if (searchView.isShown) {
                        suggestList?.visibility = View.VISIBLE
                    }
                } else {
                    suggestList?.visibility = View.INVISIBLE
                }
                return false
            }
        })

        suggestList = currentActivity.findViewById(R.id.suggest_list) as ListView

        // set suggest items(2016.01.13: only for searching all items).
        val suggestItemSearchAll = ArrayList<String>()
        suggestItemSearchAll.add(currentActivity.getString(R.string.suggest_search_all))

        // Adapter - ArrayAdapter
        val suggestListAdapter = ArrayAdapter(
                currentActivity,
                R.layout.layout_suggest_item,
                suggestItemSearchAll)

        // set on listview.
        suggestList?.adapter = suggestListAdapter
        // AdapterView.OnItemClickListener() - onItemClick(AdapterView<?> parent, View view, int position, long id).
        suggestList?.setOnItemClickListener { parent, view, position, id ->
            suggestList?.visibility = View.INVISIBLE
            setMarkersByFreeWord("")
            strLastQuery = ""
        }
        suggestList?.visibility = View.INVISIBLE

        // MenuItem.OnMenuItemClickListener() - onMenuItemClick(MenuItem item).
        toolbar.menu.findItem(R.id.update_button).setOnMenuItemClickListener { item ->
            // reload toilet datas from csv.
            loadCsvData(false, strLastQuery)
            false
        }
        toolbar.menu.findItem(R.id.show_about_button).setOnMenuItemClickListener { item ->
            // aboutページの表示.
            val intent = Intent(currentActivity, AboutAppActivity::class.java)
            currentActivity.startActivity(intent)
            true
        }
        getMap(strLastQuery)
    }

    private fun getMap(newQuery: String) {
        strLastQuery = newQuery
        // Android6.0以降なら権限確認.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions()
        } else {
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery)
        }
    }

    private fun setMarkersByFreeWord(newQuery: String) {
        isLoadingCanceled = false
        locationAccesser.clearMap()
        val dataSearcher = ToiletDataSearcher(currentActivity, this, newQuery)
        dataSearcher.execute()
    }
    private fun stopLoadingCsvData() {
        currentActivity.runOnUiThread {
            isLoadingCanceled = true
            // hide loading dialog.
            loadingPanelViewer.hide()
            dataLoader?.stopLoading()
            dataLoader?.cancel(true)
        }

    }
}

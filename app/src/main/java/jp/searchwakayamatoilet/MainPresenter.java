package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/09/07.
 * presenter for MainActivity.
 */
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.widget.SearchView;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;

import java.util.Timer;
import java.util.TimerTask;

public class MainPresenter {
    @NonNull
    private final MapManager mapManager;
    @NonNull
    private final LoadingPanelViewer loadingPanelViewer;
    @NonNull
    private final ToiletInfoAccesser toiletInfoAccesser;
    private boolean isLoadingCanceled = false;
    @NonNull
    private final TimerController timeController;
    @NonNull
    private final FragmentActivity currentActivity;

    private ListView suggestList;
    @NonNull
    private String lastQuery = "";

    private Subscription loadSubscription;
    private Subscription searchSubscription;

    public class TimerController extends TimerTask {
        private MainPresenter mainPresenter;
        public TimerController(MainPresenter setMainPresenter){
            mainPresenter = setMainPresenter;
        }
        @Override
        public void run() {
            currentActivity.runOnUiThread(()-> mapManager.moveCurrentLocation(mainPresenter));
        }
    }
    public String getLastQuery(){
        return lastQuery;
    }
    public MainPresenter(@NonNull FragmentActivity setActivity, @NonNull String setLastQuery){
        currentActivity = setActivity;
        toiletInfoAccesser = new ToiletInfoAccesser(setActivity);
        timeController = new TimerController(this);
        mapManager = new MapManager(
                (LocationManager)setActivity.getSystemService(Context.LOCATION_SERVICE), currentActivity);
        loadingPanelViewer = new LoadingPanelViewer(currentActivity);

        lastQuery = setLastQuery;
        initGui();
    }
    public void onRequestPermissionsResult(int[] grantResultNums) {
        // 権限リクエストの結果を取得する.
        if (grantResultNums[0] == PackageManager.PERMISSION_GRANTED) {
            // after being allowed permissions, get GoogleMap and start loading CSV.
            mapManager.setGoogleMap(currentActivity, this, lastQuery);
        }
    }
    public void loadToiletInfo(boolean isExistingDataUsed, String newQuery) {
        isLoadingCanceled = false;
        mapManager.clearMap();

        startLoadingToiletInfo();
        loadSubscription = toiletInfoAccesser.loadToiletData(currentActivity, isExistingDataUsed, newQuery)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<ToiletInfoClass.ToiletInfo>>() {
            @Override
            public void onCompleted() {
                // LoadingPanelを非表示にする.
                loadingPanelViewer.hide();
            }
            @Override
            public void onError(Throwable e) {
                showErrorDialog(e.getMessage());
            }
            @Override
            public void onNext(ArrayList<ToiletInfoClass.ToiletInfo> toiletInfoList){
                addMarker(toiletInfoList);
            }
        });
    }
    public void showErrorDialog(String errorMessage) {
        currentActivity.runOnUiThread (() ->{
            AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);
            alert.setTitle(currentActivity.getString(R.string.error_title));
            alert.setMessage(currentActivity.getString(R.string.error_dialog) + errorMessage);
            alert.setPositiveButton(currentActivity.getString(android.R.string.ok), null);
            alert.show();
        });
    }
    public void onResume(){
        currentActivity.runOnUiThread (() -> {
            isLoadingCanceled = true;
            // hide loading dialog.
            loadingPanelViewer.hide();
            toiletInfoAccesser.stopLoading();
        });
    }
    public void onPaused() {
        if(loadSubscription != null){
            loadSubscription.unsubscribe();
        }
        if(searchSubscription != null){
            searchSubscription.unsubscribe();
        }
    }
    public void onActivityResult(int requestCode, int resultCode){
        if(requestCode == mapManager.getRequestGpsEnable()){
            if (resultCode == Activity.RESULT_OK) {
                // get location data.
                moveCurrentLocation();
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        // 権限が許可されていない場合はリクエスト.
        if (currentActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapManager.setGoogleMap(currentActivity, this, lastQuery);
        } else {
            currentActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}
                    , R.string.request_permission);
        }
    }
    private void moveCurrentLocation() {
        // GPSをONにした直後は値が取れない場合があるのでTimerで1秒待つ.
        Timer waitGettingLocationTimer = new Timer();
        waitGettingLocationTimer.schedule(timeController, 1000L);
    }
    private void initGui() {
        Toolbar toolbar = (Toolbar)currentActivity.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);

        SearchManager searchManager = (SearchManager)currentActivity.getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.searchview);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(currentActivity.getComponentName()));

        MenuItemCompat.setActionView(searchItem, searchView);

        // hide Submit button.
        searchView.setSubmitButtonEnabled(false);

        searchView.setQueryHint(currentActivity.getString(R.string.searchview_queryhint));
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchView.setIconifiedByDefault(true);

        searchView.setOnCloseListener(()->{
                suggestList.setVisibility(View.INVISIBLE);
                return false;
            });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(@NonNull String query){
                // hide suggest items.
                suggestList.setVisibility(View.INVISIBLE);
                // search toilet name or address by input words by Submit button or EnterKey.
                setMarkersByFreeWord(query);
                lastQuery = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                // if the textarea empty, show suggest items.
                if (newText.isEmpty()) {
                    if (searchView.isShown()) {
                        suggestList.setVisibility(View.VISIBLE);
                    }
                } else {
                    suggestList.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });

        ListView suggestList = (ListView)currentActivity.findViewById(R.id.suggest_list);

        if(suggestList != null) {
            // set suggest items(2016.01.13: only for searching all items).
            ArrayList<String> suggestItemSearchAll = new ArrayList<>();
            suggestItemSearchAll.add(currentActivity.getString(R.string.suggest_search_all));

            // Adapter - ArrayAdapter
            ArrayAdapter<String> suggestListAdapter = new ArrayAdapter<>(
                    currentActivity,
                    R.layout.layout_suggest_item,
                    suggestItemSearchAll);

            // set on listview.
            suggestList.setAdapter(suggestListAdapter);
            // AdapterView.OnItemClickListener() - onItemClick(AdapterView<?> parent, View view, int position, long id).
            suggestList.setOnItemClickListener((parent, view, position, id) ->{
                    suggestList.setVisibility(View.INVISIBLE);
                    setMarkersByFreeWord("");
                    lastQuery = "";
                });
            suggestList.setVisibility(View.INVISIBLE);
        }

        // MenuItem.OnMenuItemClickListener() - onMenuItemClick(MenuItem item).
        toolbar.getMenu().findItem(R.id.update_button).setOnMenuItemClickListener(item ->{
                // reload toilet datas from csv.
                loadToiletInfo(false, lastQuery);
                return false;
            });
        toolbar.getMenu().findItem(R.id.show_about_button).setOnMenuItemClickListener ( item -> {
                // aboutページの表示.
                Intent intent = new Intent(currentActivity, AboutAppActivity.class);
                currentActivity.startActivity(intent);
                return true;
            });
        getMap(lastQuery);
    }
    private void startLoadingToiletInfo() {
        // show loading dialog.
        currentActivity.runOnUiThread(() -> loadingPanelViewer.show());
    }
    private void getMap(String newQuery) {
        lastQuery = newQuery;
        // Android6.0以降なら権限確認.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        } else {
            mapManager.setGoogleMap(currentActivity, this, lastQuery);
        }
    }

    private void setMarkersByFreeWord(String newQuery) {
        isLoadingCanceled = false;
        mapManager.clearMap();
        searchSubscription = toiletInfoAccesser.searchToiletData(newQuery)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<ToiletInfoClass.ToiletInfo>>() {
                    @Override
                    public void onCompleted() {
                    }
                    @Override
                    public void onError(Throwable e) {
                        showErrorDialog(e.getMessage());
                    }
                    @Override
                    public void onNext(ArrayList<ToiletInfoClass.ToiletInfo> toiletInfoList){
                        if(toiletInfoList.size() <= 0){
                            // show toast on getting no results.
                            showToast(R.string.toast_no_results);
                        }
                        else{
                            addMarker(toiletInfoList);
                        }
                    }
        });
    }
    private void showToast(int messageNum) {
        // Runnable() - run().
        currentActivity.runOnUiThread(() -> Toast.makeText(currentActivity, messageNum, Toast.LENGTH_SHORT).show());
    }
    private void addMarker(ArrayList<ToiletInfoClass.ToiletInfo> toiletInfoModelList) {
        currentActivity.runOnUiThread(()-> {
            for (ToiletInfoClass.ToiletInfo toiletInfo: toiletInfoModelList) {
                if (isLoadingCanceled) {
                    break;
                }
                String newSnippet = currentActivity.getString(R.string.marker_address);
                newSnippet += toiletInfo.address;
                newSnippet += currentActivity.getString(R.string.marker_availabletime);
                newSnippet += toiletInfo.availableTime;

                mapManager.addMarker(toiletInfo.toiletName, toiletInfo.latitude, toiletInfo.longitude, newSnippet);
            }
        });
    }
}

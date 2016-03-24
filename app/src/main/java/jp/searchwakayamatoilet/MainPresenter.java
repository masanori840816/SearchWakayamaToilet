package jp.searchwakayamatoilet;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by masanori on 2016/01/24.
 */
public class MainPresenter {
    //private Activity currentActivity;
    private final FragmentActivity currentActivity;
    private final LocationAccesser locationAccesser;
    private final LoadingPanelViewer loadingPanelViewer;
    private ToiletDataLoader dataLoader;
    private boolean isLoadingCanceled;

    private TimerController timeController;

    private ListView suggestList;
    private String strLastQuery;

    private final static AboutAppFragment aboutAppFragment = new AboutAppFragment();

    public String getStrLastQuery(){
        return strLastQuery;
    }
    public MainPresenter(FragmentActivity newActivity, String lastQuery){
        currentActivity = newActivity;
        timeController = new TimerController(this);
        locationAccesser = new LocationAccesser(
                (LocationManager) newActivity.getSystemService(Context.LOCATION_SERVICE)
                , newActivity);
        loadingPanelViewer = new LoadingPanelViewer(newActivity, this);

        strLastQuery = lastQuery;
        init();
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(){
        // 権限が許可されていない場合はリクエスト.
        if (currentActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery);
        } else {
            currentActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, R.string.request_permission);
        }
    }
    public void onRequestPermissionsResult(int[] intGrantResults) {
        // 権限リクエストの結果を取得する.
        if (intGrantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // after being allowed permissions, get GoogleMap and start loading CSV.
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery);
        }
    }
    public void loadCsvData(boolean isExistingDataUsed, String newQuery){
        isLoadingCanceled = false;
        locationAccesser.clearMap();
        dataLoader = new ToiletDataLoader(currentActivity, isExistingDataUsed, this, newQuery);
        dataLoader.execute();
    }
    public void moveCurrentLocation(){
        // GPSをONにした直後は値が取れない場合があるのでTimerで1秒待つ.
        Timer tmrGettingLocationTimer = new Timer();
        tmrGettingLocationTimer.schedule(timeController, 1000L);
    }
    public class TimerController extends TimerTask{
        private final MainPresenter mainPresenter;
        public TimerController(MainPresenter presenter){
            mainPresenter = presenter;
        }
        @Override
        public void run() {
            // Runnable() - run().
            currentActivity.runOnUiThread(
                    () -> {
                        locationAccesser.moveCurrentLocation(mainPresenter);
                    });
        }
    }
    public void startLoadingCsvData(){
        currentActivity.runOnUiThread(
                () -> {
                    // show loading dialog.
                    loadingPanelViewer.show();
                });
    }
    public void stopLoadingCsvData(){
        if(currentActivity == null){
            return;
        }
        // Runnable() - run().
        currentActivity.runOnUiThread(
                () -> {
                    isLoadingCanceled = true;
                    // hide loading dialog.
                    if(loadingPanelViewer != null){
                        loadingPanelViewer.hide();
                    }
                    if(dataLoader != null){
                        dataLoader.stopLoading();
                        dataLoader.cancel(true);
                    }
                });

    }
    public void addMarker(ArrayList<DatabaseAccesser.ToiletInfoModel> toiletInfoModelList){
        currentActivity.runOnUiThread(
                () -> {
                    for (DatabaseAccesser.ToiletInfoModel toiletInfo : toiletInfoModelList) {
                        if(isLoadingCanceled){
                            break;
                        }
                        String newSnippet = currentActivity.getString(R.string.marker_address);
                        newSnippet += toiletInfo.address;
                        newSnippet += currentActivity.getString(R.string.marker_availabletime);
                        newSnippet += toiletInfo.availableTime;

                        locationAccesser.addMarker(toiletInfo.toiletName, toiletInfo.latitude, toiletInfo.longitude, newSnippet);
                    }
                });
    }
    public void showToast(int messageNum){
        // Runnable() - run().
        currentActivity.runOnUiThread(
                () -> {
                    Toast.makeText(currentActivity, messageNum, Toast.LENGTH_SHORT).show();
                });
    }
    public void showErrorDialog(String errorMessage){
        // Runnable() - run().
        currentActivity.runOnUiThread(
                () -> {
                    AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);
                    alert.setTitle(currentActivity.getString(R.string.error_title));
                    alert.setMessage(currentActivity.getString(R.string.error_dialog) + errorMessage);
                    // DialogInterface.OnClickListener() - onClick(DialogInterface dialog, int which).
                    alert.setPositiveButton(currentActivity.getString(android.R.string.ok), null);
                    alert.show();
                });
    }
    public void onPaused(){
        // バックグラウンドでは処理を止める.
    }
    public boolean onOptionsItemSelected(int itemId){
        switch (itemId){
            case android.R.id.home:
                if(aboutAppFragment != null){
                    FragmentTransaction transaction = currentActivity.getSupportFragmentManager().beginTransaction();
                    transaction.remove(aboutAppFragment);
                    transaction.commit();
                    return true;
                }
                break;
        }
        return false;
    }
    private void init(){
        Toolbar toolbar = (Toolbar) currentActivity.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(toolbar.getMenu().findItem(R.id.searchview));
        // hide Submit button.
        searchView.setSubmitButtonEnabled(false);

        searchView.setQueryHint(currentActivity.getString(R.string.searchview_queryhint));
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        // SearchView.OnCloseListener() - onClose().
        searchView.setOnCloseListener(
                () -> {
                    suggestList.setVisibility(View.INVISIBLE);
                    return false;
                });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // hide suggest items.
                suggestList.setVisibility(View.INVISIBLE);
                // search toilet name or address by input words by Submit button or EnterKey.
                setMarkersByFreeWord(query);
                strLastQuery = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // if the textarea empty, show suggest items.
                if (newText.equals("")) {
                    if(searchView.isShown()){
                        suggestList.setVisibility(View.VISIBLE);
                    }
                } else {
                    suggestList.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });

        suggestList = (ListView) currentActivity.findViewById(R.id.suggest_list);

        // set suggest items(2016.01.13: only for searching all items).
        ArrayList<String> suggestItemSearchAll = new ArrayList<>();
        suggestItemSearchAll.add(currentActivity.getString(R.string.suggest_search_all));

        // Adapter - ArrayAdapter
        ArrayAdapter<String> suggestListAdapter = new ArrayAdapter<>(
                currentActivity,
                R.layout.layout_suggest_item,
                suggestItemSearchAll
        );

        // set on listview.
        suggestList.setAdapter(suggestListAdapter);
        // AdapterView.OnItemClickListener() - onItemClick(AdapterView<?> parent, View view, int position, long id).
        suggestList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    suggestList.setVisibility(View.INVISIBLE);
                    setMarkersByFreeWord("");
                    strLastQuery = "";
                });
        suggestList.setVisibility(View.INVISIBLE);

        // MenuItem.OnMenuItemClickListener() - onMenuItemClick(MenuItem item).
        toolbar.getMenu().findItem(R.id.update_button).setOnMenuItemClickListener(
                item -> {
                    // reload toilet datas from csv.
                    loadCsvData(false, strLastQuery);
                    return false;
                });
        toolbar.getMenu().findItem(R.id.show_about_button).setOnMenuItemClickListener(
                item -> {
                    // aboutページの表示.
                    FragmentTransaction transaction = currentActivity.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.main_container, aboutAppFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    return true;
                });
        getMap(strLastQuery);
    }
    private void getMap(String newQuery){
        strLastQuery = newQuery;
        // Android6.0以降なら権限確認.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions();
        } else {
            locationAccesser.getGoogleMap(currentActivity, this, strLastQuery);
        }
    }
    private void setMarkersByFreeWord(String newQuery) {
        isLoadingCanceled = false;
        locationAccesser.clearMap();
        ToiletDataSearcher dataSearcher = new ToiletDataSearcher(currentActivity, this, newQuery);
        dataSearcher.execute();
    }
}

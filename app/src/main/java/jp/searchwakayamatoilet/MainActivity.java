/**
 * Created by Masanori on 2015/12/09.
 * this class is main controller of this application.
 */
package jp.searchwakayamatoilet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements IPageView {

    private MainActivity mainActivity;

    private TimerController mTimeController;
    private Timer mTmrGettingLocationTimer;

    private MainPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this, this);

        mainActivity = this;

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
                presenter.setMarkersByFreeWord(searchWord);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 入力される度に呼び出される
                return false;
            }
        });
        // MenuItem.OnMenuItemClickListener() - onMenuItemClick(MenuItem item).
        _toolbar.getMenu().findItem(R.id.updatebutton).setOnMenuItemClickListener(
                item -> {
                    // reload toilet datas from csv.
                    presenter.loadCsvData(false);
                    return false;
                });
        presenter.getMap();
    }
    @Override
    public void onRequestPermissionsResult(int intRequestCode, String[] strPermissions, int[] intGrantResults) {
        // 権限リクエストの結果を取得する.
        if (intRequestCode == R.string.request_permission) {
            presenter.onRequestPermissionsResult(intGrantResults);
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
        // if toilet datas are loading from csv, stop loading.
        presenter.stopLoadingCsvData();
        super.onPause();
    }
    public class TimerController extends TimerTask{
        @Override
        public void run() {
            // Runnable() - run().
            mainActivity.runOnUiThread(
                    () -> {
                        // get location data.
                        presenter.moveCurrentLocation();
                    });
        }
    }
    public void stopLoading(){
        presenter.stopLoadingCsvData();
    }
    public void addMarker(String strToiletName, double dblLatitude, double dblLongitude){
        presenter.addMarker(strToiletName, dblLatitude, dblLongitude);
    }
}

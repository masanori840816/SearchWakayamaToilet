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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity{

    private MainActivity mainActivity;

    private TimerController timeController;
    private Timer tmrGettingLocationTimer;

    private MainPresenter presenter;
    private ListView suggestList;
    private ArrayList<String> suggestItemSearchAll;
    private ArrayList<String> suggestItems;
    private ArrayAdapter<String> suggestListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);

        mainActivity = this;

        timeController = new TimerController();

        Toolbar _toolbar = (Toolbar) findViewById(R.id.toolbar);
        _toolbar.inflateMenu(R.menu.menu_main);

        SearchView _searchView = (SearchView) MenuItemCompat.getActionView(_toolbar.getMenu().findItem(R.id.searchview));
        // hide Submit button.
        _searchView.setSubmitButtonEnabled(false);

        _searchView.setQueryHint(getString(R.string.searchview_queryhint));

        // SearchView.OnCloseListener() - onClose().
        _searchView.setOnCloseListener(
                () -> {
                    suggestList.setVisibility(View.INVISIBLE);
                    return false;
                });
        _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                // hide suggest items.
                suggestList.setVisibility(View.INVISIBLE);
                // search toilet name or address by input words by Submit button or EnterKey.
                presenter.setMarkersByFreeWord(searchWord);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // if the textarea empty, show suggest items.
                if (newText.equals("")) {
                    suggestList.setVisibility(View.VISIBLE);
                } else {
                    suggestList.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });
        suggestList = (ListView) findViewById(R.id.suggest_list);

        // set suggest items(2016.01.13: only for searching all items).
        suggestItemSearchAll = new ArrayList<>();
        suggestItemSearchAll.add(getString(R.string.suggest_search_all));

        suggestItems = suggestItemSearchAll;

        // Adapter - ArrayAdapter
        suggestListAdapter = new ArrayAdapter<>(
                this,
                R.layout.layout_suggest_item,
                suggestItems
        );

        // set on listview.
        suggestList.setAdapter(suggestListAdapter);
        // AdapterView.OnItemClickListener() - onItemClick(AdapterView<?> parent, View view, int position, long id).
        suggestList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    suggestList.setVisibility(View.INVISIBLE);
                    presenter.setMarkersByFreeWord("");
                });
        suggestList.setVisibility(View.INVISIBLE);

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
                    tmrGettingLocationTimer = new Timer();
                    tmrGettingLocationTimer.schedule(timeController, 1000L);
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
}

/**
 * Created by Masanori on 2015/12/09.
 * this class is main controller of this application.
 */
package jp.searchwakayamatoilet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements AboutAppFragment.OnFragmentInteractionListener{

    private MainPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String _lastQuery = "";
        if(savedInstanceState != null){
            _lastQuery = savedInstanceState.getString(getString(R.string.saveinstance_key_last_query));
        }

        presenter = new MainPresenter(this, _lastQuery);
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
                    // get location data.
                    presenter.moveCurrentLocation();
                }
                break;
        }
    }

    @Override
    public void onPause(){
        // if toilet datas are loading from csv, stop loading.
        if(presenter != null) {
            presenter.onPaused();
        }
        super.onPause();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(presenter.onOptionsItemSelected(item.getItemId())){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveinstance_key_last_query), presenter.getStrLastQuery());
    }
}

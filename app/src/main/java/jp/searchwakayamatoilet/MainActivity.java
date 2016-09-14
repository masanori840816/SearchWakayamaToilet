package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/07/30.
 * main page activity.
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity{
    private MainPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);

        String lastQuery = "";

        if(savedInstanceState != null){
            // 最後に入力されていたQueryがあればセット.
            lastQuery = savedInstanceState.getString(getString(R.string.saveinstance_key_last_query));
            if(lastQuery == null){
                lastQuery = "";
            }
        }
        presenter = new MainPresenter(this, lastQuery);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 権限リクエストの結果を取得する.
        if (requestCode == R.string.request_permission) {
            presenter.onRequestPermissionsResult(grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode);
    }
    @Override
    public void onPause() {
        // if toilet datas are loading from csv, stop loading.
        presenter.onPaused();
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveinstance_key_last_query), presenter.getLastQuery());
    }
}

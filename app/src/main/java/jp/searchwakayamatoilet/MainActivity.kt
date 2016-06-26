/**
 * Created by Masanori on 2015/12/09.
 * this is main page activity.
 */
package jp.searchwakayamatoilet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import android.util.Log

class MainActivity : AppCompatActivity() {

    lateinit private var presenter: MainPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 最後に入力されていたQueryがあればセット,
        val lastQuery = savedInstanceState?.getString(getString(R.string.saveinstance_key_last_query))

        presenter = MainPresenter(this, lastQuery)
    }

    override fun onRequestPermissionsResult(intRequestCode: Int, strPermissions: Array<String>, intGrantResults: IntArray) {
        // 権限リクエストの結果を取得する.
        if (intRequestCode == R.string.request_permission) {
            presenter.onRequestPermissionsResult(intGrantResults)
        } else {
            super.onRequestPermissionsResult(intRequestCode, strPermissions, intGrantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            R.string.request_enable_location -> if (resultCode == Activity.RESULT_OK) {
                // get location data.
                presenter.moveCurrentLocation()
            }
        }
    }

    public override fun onPause() {
        // if toilet datas are loading from csv, stop loading.
        presenter.onPaused()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(getString(R.string.saveinstance_key_last_query), presenter.strLastQuery)
    }
}

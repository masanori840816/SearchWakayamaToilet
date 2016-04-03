/**
 * Created by Masanori on 2015/12/09.
 * this class is main controller of this application.
 */
package jp.searchwakayamatoilet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import android.util.Log
import android.view.MenuItem

class MainActivity : AppCompatActivity(), AboutAppFragment.OnFragmentInteractionListener {

    private var presenter: MainPresenter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var _lastQuery = ""
        if (savedInstanceState != null) {
            _lastQuery = savedInstanceState.getString(getString(R.string.saveinstance_key_last_query))
        }

        presenter = MainPresenter(this, _lastQuery)
    }

    override fun onRequestPermissionsResult(intRequestCode: Int, strPermissions: Array<String>, intGrantResults: IntArray) {
        // 権限リクエストの結果を取得する.
        if (intRequestCode == R.string.request_permission) {
            presenter!!.onRequestPermissionsResult(intGrantResults)
        } else {
            super.onRequestPermissionsResult(intRequestCode, strPermissions, intGrantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            R.string.request_enable_location -> if (resultCode == Activity.RESULT_OK) {
                // get location data.
                presenter!!.moveCurrentLocation()
            }
        }
    }

    public override fun onPause() {
        // if toilet datas are loading from csv, stop loading.
        if (presenter != null) {
            presenter!!.onPaused()
        }
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (presenter!!.onOptionsItemSelected(item.itemId)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(getString(R.string.saveinstance_key_last_query), presenter!!.strLastQuery)
    }
}

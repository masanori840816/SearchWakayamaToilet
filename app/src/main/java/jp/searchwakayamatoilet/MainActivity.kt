package jp.searchwakayamatoilet

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import android.support.v4.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
import java.util.regex.Pattern

class MainActivity : FragmentActivity() {

    private var mMap: GoogleMap? = null
    private var mMain: MainActivity? = null
    private var mStrToiletName: String? = null
    private var mDblLatitude: Double = 0.toDouble()
    private var mDblLongitude: Double = 0.toDouble()
    private final val REQUEST_PERMISSIONS: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMain = this

        // Android6.0以降なら権限確認.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions()
        }
        else{
            this.getNewMap()

            this.loadCsv()
        }

    }
    private fun requestPermissions(){
        // 権限が許可されていない場合はリクエスト.
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.getNewMap()
            this.loadCsv()
        }else{
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // 権限リクエストの結果を取得する.
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // after being allowed permissions, get GoogleMap and start loading CSV.
                this.getNewMap()
                this.loadCsv()
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private fun getNewMap(){
        // get GoogleMap instance.
        if (mMap != null) {
            return
        }
        // マップの表示.
        var mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(
                {
                    mMap = it
                    mMap!!.isMyLocationEnabled = true})
    }
    private fun loadCsv(){
        val handlerThread = HandlerThread("AddMarker")
        handlerThread.start()

        val handler = Handler(handlerThread.looper)
        handler.post {
            val geocoder = Geocoder(mMain, Locale.getDefault())
            val asmAsset = mMain!!.resources.assets
            try {
                val ipsInput = asmAsset.open("toilet-map.csv")
                val inputStreamReader = InputStreamReader(ipsInput)
                val bufferReader = BufferedReader(inputStreamReader)
                var strLine: String?
                var strSplited: Array<String>
                val p = Pattern.compile("^[0-9]+")

                while (true) {
                    strLine = bufferReader.readLine()
                    if(strLine == null){
                        break
                    }

                    // とりあえず数値から始まっている行のみ
                    if (p.matcher(strLine).find()) {
                        // とりあえずSplit後に4件以上データがある行のみ.
                        strSplited = strLine.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        if (strSplited.size >= 4) {
                            // とりあえず名称と住所のみ.
                            val addrList = geocoder.getFromLocationName(strSplited[3], 1)
                            if (addrList.isEmpty()) {
                                Log.d("swtSearch", "list is empty")
                            } else {
                                val address = addrList[0]

                                mMain!!.mStrToiletName = strSplited[1]
                                mMain!!.mDblLatitude = address.latitude
                                mMain!!.mDblLongitude = address.longitude

                                getCsvHandler.sendEmptyMessage(1)
                            }
                        }
                    }
                }
                bufferReader.close()
                ipsInput.close()

            } catch (e: IOException) {
                Log.d("swtSearch", "IOException 発生")
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private val getCsvHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            addMarker()
        }
    }

    private fun addMarker() {
        if (mMap != null) {
            // 表示したマップにマーカーを追加する.
            mMap!!.addMarker(MarkerOptions().position(
                    LatLng(mMain!!.mDblLatitude, mMain!!.mDblLongitude)).title(mMain!!.mStrToiletName))

        }
    }
}

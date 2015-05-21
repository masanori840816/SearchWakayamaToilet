package jp.searchwakayamatoilet;

import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity{

    private GoogleMap mMap;
    private MainActivity mMain;
    private String mStrToiletName;
    private double mDblLatitude;
    private double mDblLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMain = this;

        if (mMap == null)
        {
            // マップの表示.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMyLocationEnabled(true);
        }

        HandlerThread handlerThread = new HandlerThread("AddMarker");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(mMain, Locale.getDefault());
                AssetManager asmAsset = mMain.getResources().getAssets();
                try {
                    InputStream ipsInput = asmAsset.open("toilet-map.csv");
                    InputStreamReader inputStreamReader = new InputStreamReader(ipsInput);
                    BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                    String strLine = "";
                    String[] strSplited;
                    Pattern p = Pattern.compile("^[0-9]+");

                    while ((strLine = bufferReader.readLine()) != null) {
                        // とりあえず数値から始まっている行のみ
                        if (p.matcher(strLine).find()) {
                            // とりあえずSplit後に4件以上データがある行のみ.
                            strSplited = strLine.split(",");
                            if (strSplited.length >= 4) {
                                // とりあえず名称と住所のみ.
                                List<Address> addrList = geocoder.getFromLocationName(strSplited[3], 1);
                                if (addrList.isEmpty()) {
                                    Log.d("swtSearch", "list is empty");
                                } else {
                                    Address address = addrList.get(0);

                                    mMain.mStrToiletName = strSplited[1];
                                    mMain.mDblLatitude = address.getLatitude();
                                    mMain.mDblLongitude = address.getLongitude();

                                    getCsvHandler.sendEmptyMessage(1);
                                }
                            }
                        }
                    }
                    bufferReader.close();
                    ipsInput.close();

                } catch (IOException e) {
                    Log.d("swtSearch", "IOException 発生");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private Handler getCsvHandler = new Handler() {
        public void handleMessage(Message msg) {
            addMarker();
        }
    };
    public void addMarker()
    {
        if (mMap != null)
        {
            // 表示したマップにマーカーを追加する.
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(mMain.mDblLatitude, mMain.mDblLongitude)).title(mMain.mStrToiletName));

        }
    }
}

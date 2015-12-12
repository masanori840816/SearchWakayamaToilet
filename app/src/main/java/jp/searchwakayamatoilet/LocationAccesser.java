package jp.searchwakayamatoilet;

import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by Masanori on 2015/12/12.
 */
public class LocationAccesser {
    private NetworkAccesser mNetworkAccesser;
    public void initialize(final MainActivity mainActivity){
        mNetworkAccesser = new NetworkAccesser();
        mNetworkAccesser.initialize(mainActivity);
    }
    public void pause(final MainActivity mainActivity){
        // ネットワーク状態の監視をストップ.
        mainActivity.unregisterReceiver(mNetworkAccesser);
    }
    public void resume(final MainActivity mainActivit){
        // ネットワーク状態の監視を再開.
        mainActivit.registerReceiver(mNetworkAccesser, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
    }
    public void loadCsvData(final MainActivity mainActivity){
        if(! mNetworkAccesser.checkIsNetworkConnected()){
            return;
        }

        /*
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 34992, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.checkIsGpsEnable(mGoogleApiClient, mMainActivity);
         */

        HandlerThread handlerThread = new HandlerThread("AddMarker");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(mainActivity, Locale.getDefault());
                AssetManager asmAsset = mainActivity.getResources().getAssets();
                try {
                    // CSVの読み込み.
                    InputStream ipsInput = asmAsset.open("toilet-map.csv");
                    InputStreamReader inputStreamReader = new InputStreamReader(ipsInput);
                    BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                    String strLine = "";
                    String[] strSplited;
                    Pattern p = Pattern.compile("^[0-9]+");
                    // 1行ずつ読み込む.
                    while ((strLine = bufferReader.readLine()) != null) {
                        // とりあえず数値から始まっている行のみ
                        if (p.matcher(strLine).find()) {
                            // とりあえずSplit後に4件以上データがある行のみ.
                            strSplited = strLine.split(",");
                            if (strSplited.length >= 4) {
                                // とりあえず名称と住所のみ.
                                List addressList = geocoder.getFromLocationName(strSplited[3], 1);
                                if (addressList.isEmpty()) {
                                    Log.d("swtSearch", "list is empty");
                                } else {
                                    Address address = (Address) addressList.get(0);
                                    // UIスレッドで取得したデータを受け取れるようにする.
                                    MainActivity.setNewMarker(strSplited[1], address.getLatitude(), address.getLongitude());
                                }
                            }
                        }
                    }
                    bufferReader.close();
                    ipsInput.close();

                } catch (IOException e) {
                    Log.d("swtSearch", "Exception 発生");
                }
            }
        });
    }
}

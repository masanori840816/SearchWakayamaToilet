package jp.searchwakayamatoilet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Masanori on 2015/12/12.
 */
public class NetworkAccesser extends BroadcastReceiver {

    private MainActivity mMainActivity;
    private static NetworkAccesser mNetworkAccesser;
    public static void initialize(MainActivity newActivity){
        mNetworkAccesser = new NetworkAccesser();
        mNetworkAccesser.mMainActivity = newActivity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        mNetworkAccesser.mMainActivity.loadCsv();
    }
}

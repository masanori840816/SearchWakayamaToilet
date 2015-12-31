/**
 * Created by Masanori on 2015/12/12.
 * this class controls Networks.
 */
package jp.searchwakayamatoilet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkAccesser extends BroadcastReceiver {
    private MainActivity mMainActivity;

    public void initialize(final MainActivity mainActivity){
        mMainActivity = mainActivity;
    }
    public boolean checkIsNetworkConnected(){
        NetworkInfo networkInfo = mMainActivity.getConnectivityManager()
                .getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        mMainActivity.showToastNoNetworks();
        return false;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        mMainActivity.networkStatusChanged();
    }
}

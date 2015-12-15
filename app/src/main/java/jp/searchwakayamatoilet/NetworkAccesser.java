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

public class NetworkAccesser extends BroadcastReceiver {
    public boolean checkIsNetworkConnected(ConnectivityManager connectivityManager){
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        return false;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.networkStatusChanged();
    }
}

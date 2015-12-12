package jp.searchwakayamatoilet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Masanori on 2015/12/12.
 */
public class NetworkAccesser extends BroadcastReceiver {
    private MainActivity mMainActivity;
    public void initialize(final MainActivity mainActivity){
        mMainActivity = mainActivity;
    }
    public boolean checkIsNetworkConnected(){
        NetworkInfo networkInfo = ((ConnectivityManager) mMainActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        MainActivity.showToastNoNetworks();
        return false;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.networkStatusChanged();
    }
}

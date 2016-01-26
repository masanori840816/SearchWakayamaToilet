/**
 * Created by Masanori on 2015/12/12.
 * this class controls Networks.
 */
package jp.searchwakayamatoilet;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkAccesser{
    public boolean checkIsNetworkConnected(final Activity currentActivity){
        ConnectivityManager connectivity = (ConnectivityManager)currentActivity
                .getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        // ネットワークにつながっていなければToast表示.
        Toast.makeText(currentActivity, R.string.toast_no_networks, Toast.LENGTH_SHORT).show();
        return false;
    }
}

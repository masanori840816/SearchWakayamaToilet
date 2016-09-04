package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/09/05.
 * this shows loading indicator.
 */
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
public class LoadingPanelViewer {
    @NonNull
    private ProgressDialog progressDialog;

    public LoadingPanelViewer(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setOnKeyListener((DialogInterface dialog, int keyCode, KeyEvent event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                RxBusProvider.Companion.getInstance().send(new LoadingPanelEvent(true));
            }
            return false;
        });
        progressDialog.setCancelable(false);
    }
    public void show(){
        progressDialog.show();
        progressDialog.setContentView(R.layout.layout_loading);
    }

    public void hide() {
        progressDialog.dismiss();
    }
}

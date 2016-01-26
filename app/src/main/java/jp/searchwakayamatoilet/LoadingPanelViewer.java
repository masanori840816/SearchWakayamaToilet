package jp.searchwakayamatoilet;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;

/**
 * Created by masanori on 2016/01/21.
 */
public class LoadingPanelViewer {
    private ProgressDialog progressDialog;

    public LoadingPanelViewer(Context context, IPageView currentPage){
        progressDialog = new ProgressDialog(context);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // DialogInterface.OnKeyListener() - onKey(DialogInterface dialog, int keyCode, KeyEvent event).
        progressDialog.setOnKeyListener(
                (dialog, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        currentPage.stopLoading();
                    }
                    return false;
                });
        progressDialog.setCancelable(false);
    }
    public void show(){
        progressDialog.show();
        progressDialog.setContentView(R.layout.layout_loading);

    }
    public void hide(){
        progressDialog.dismiss();
    }
}

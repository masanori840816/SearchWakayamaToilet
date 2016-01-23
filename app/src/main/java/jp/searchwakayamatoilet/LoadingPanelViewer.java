package jp.searchwakayamatoilet;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

/**
 * Created by masanori on 2016/01/21.
 */
public class LoadingPanelViewer {
    Context mContext;
    ProgressDialog mProgressDialog;

    public LoadingPanelViewer(Context context){
        mContext = context;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    public void show(){
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.layout_loading);
        mProgressDialog.setCancelable(false);
    }
    public void close(){
        mProgressDialog.dismiss();
    }
}

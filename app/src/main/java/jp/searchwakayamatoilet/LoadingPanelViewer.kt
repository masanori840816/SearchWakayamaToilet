package jp.searchwakayamatoilet

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent

/**
 * Created by masanori on 2016/01/21.
 * for showing loading indicator.
 */
class LoadingPanelViewer(context: Context) {
    inner class LoadinPanelEvent(willLoadingCsvBeStopped: Boolean){
        private var isLoadingCsvStopped: Boolean
        init{
            isLoadingCsvStopped = willLoadingCsvBeStopped
        }
        fun getIsLoadingCsvStopped(): Boolean{
            return isLoadingCsvStopped
        }
    }
    private val progressDialog: ProgressDialog

    init {
        progressDialog = ProgressDialog(context)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.setOnKeyListener{
            dialog: DialogInterface, keyCode: Int, event: KeyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    RxBusProvider.getInstance().send(LoadinPanelEvent(true))
                }
                false
        }
        progressDialog.setCancelable(false)
    }

    fun show() {
        progressDialog.show()
        progressDialog.setContentView(R.layout.layout_loading)
    }

    fun hide() {
        progressDialog.dismiss()
    }
}

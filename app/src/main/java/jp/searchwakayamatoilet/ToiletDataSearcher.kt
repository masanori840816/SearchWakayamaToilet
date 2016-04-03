package jp.searchwakayamatoilet

import android.app.Activity
import android.os.AsyncTask

import java.util.ArrayList

/**
 * Created by masanori on 2016/02/19.
 * get toilet info on background.
 */
class ToiletDataSearcher(newActivity: Activity, private val currentPresenter: MainPresenter, private val strQuery: String) : AsyncTask<Void, Void, Int>() {
    private val dbAccesser: DatabaseAccesser

    init {
        dbAccesser = DatabaseAccesser(newActivity)
    }

    override fun doInBackground(vararg params: Void): Int? {
        dbAccesser.setSearchCriteriaFromFreeWord(strQuery)
        val aryToiletInfo = dbAccesser.search(dbAccesser.writableDatabase)

        if (aryToiletInfo.size <= 0) {
            // show toast on getting no results.
            currentPresenter.showToast(R.string.toast_no_results)
        } else {
            // Add marker on UI Thread.
            currentPresenter.addMarker(aryToiletInfo)
        }
        return 0
    }
}

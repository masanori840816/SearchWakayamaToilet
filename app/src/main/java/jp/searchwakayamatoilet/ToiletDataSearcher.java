package jp.searchwakayamatoilet;

import android.app.Activity;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by masanori on 2016/02/19.
 */
public class ToiletDataSearcher extends AsyncTask<Void, Void, Integer> {

    private DatabaseAccesser.ToiletInfoModel toiletInfoModel;
    private DatabaseAccesser dbAccesser;
    private MainPresenter currentPresenter;
    private String strQuery;

    public ToiletDataSearcher(Activity newActivity, MainPresenter newPresenter, String strNewQuery){
        currentPresenter = newPresenter;
        dbAccesser = new DatabaseAccesser(newActivity);
        toiletInfoModel = dbAccesser.new ToiletInfoModel();
        strQuery = strNewQuery;
    }
    @Override
    protected Integer doInBackground(Void... params) {
        dbAccesser.setSearchCriteriaFromFreeWord(strQuery);
        ArrayList<DatabaseAccesser.ToiletInfoModel> aryToiletInfo = dbAccesser.search(dbAccesser.getWritableDatabase());

        if (aryToiletInfo == null
                || aryToiletInfo.size() <= 0) {
            // show toast on getting no results.
            currentPresenter.showToast(R.string.toast_no_results);
        }
        else{
            // Add marker on UI Thread.
            currentPresenter.addMarker(aryToiletInfo);
        }
        return 0;
    }
}

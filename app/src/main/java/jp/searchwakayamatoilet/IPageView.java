package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/01/25.
 */
public interface IPageView {
    void stopLoading();
    void addMarker(String strToiletName, double dblLatitude, double dblLongitude);
}

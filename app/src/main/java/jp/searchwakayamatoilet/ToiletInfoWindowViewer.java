package jp.searchwakayamatoilet;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
/**
 * Created by masanori on 2016/02/02.
 */
public class ToiletInfoWindowViewer implements GoogleMap.InfoWindowAdapter {
    private final View infoWindowView;
    public ToiletInfoWindowViewer(Activity newActivity){
        infoWindowView = newActivity.getLayoutInflater().inflate(R.layout.layout_marker_window, null);
    }
    @Override
    public View getInfoWindow(Marker marker) {
        ((TextView) infoWindowView.findViewById(R.id.marker_infowindow_title))
                .setText(marker.getTitle());
        ((TextView) infoWindowView.findViewById(R.id.marker_infowindow_snippet))
                .setText(marker.getSnippet());

        return infoWindowView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}

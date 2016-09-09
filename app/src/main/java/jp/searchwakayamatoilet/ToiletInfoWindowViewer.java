package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/09/06.
 * set texts to windows of marker on google map.
 */
import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class ToiletInfoWindowViewer implements GoogleMap.InfoWindowAdapter{
    private View infoWindowView;
    public ToiletInfoWindowViewer(@NonNull Activity newActivity){
        infoWindowView = View.inflate(newActivity.getApplicationContext(), R.layout.layout_marker_window, null);
    }
    @Override
    public View getInfoWindow(Marker marker){
        ((TextView)infoWindowView.findViewById(R.id.marker_infowindow_title)).setText(marker.getTitle());
        ((TextView)infoWindowView.findViewById(R.id.marker_infowindow_snippet)).setText(marker.getSnippet());

        return infoWindowView;
    }
    @Override
    public View getInfoContents(Marker marker){
        return null;
    }
}

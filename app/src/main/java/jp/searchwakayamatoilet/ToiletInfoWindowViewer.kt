package jp.searchwakayamatoilet

import android.app.Activity
import android.databinding.DataBindingUtil
import android.util.Log
import android.view.View
import android.widget.TextView

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

import jp.searchwakayamatoilet.databinding.LayoutMarkerWindowBinding
/**
 * Created by masanori on 2016/02/02.
 * set texts to windows of marker on google map.
 */
class ToiletInfoWindowViewer(newActivity: Activity) : GoogleMap.InfoWindowAdapter {
   // private val infoWindowView: View?
    private val binding: LayoutMarkerWindowBinding

    init {
        binding = DataBindingUtil.setContentView(newActivity, R.layout.layout_marker_window)
     //   infoWindowView = newActivity.layoutInflater?.inflate(R.layout.layout_marker_window, null)
    }

    override fun getInfoWindow(marker: Marker): View {
        binding.markerInfowindowTitle.text = marker.title
        binding.markerInfowindowSnippet.text = marker.snippet
        //(infoWindowView?.findViewById(R.id.marker_infowindow_title) as TextView).text = marker.title
        //(infoWindowView?.findViewById(R.id.marker_infowindow_snippet) as TextView).text = marker.snippet

        //return infoWindowView
        return binding.root
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}

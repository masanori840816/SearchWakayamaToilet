package jp.searchwakayamatoilet;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AboutAppFragment extends Fragment {

    private OnFragmentInteractionListener interactionListener;

    public AboutAppFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            this.setupWindowAnimations();
        }

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        this.setAllowEnterTransitionOverlap(true);
        Fade fade = new Fade();
        fade.setDuration(500);
        // animation on returning by home button.
        this.setExitTransition(fade);
        // animation on returning by backkey of device.
        this.setReturnTransition(fade);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about_app, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        // hide home button's title.
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

        // add links to textviews.
        TextView _projectView = (TextView) view.findViewById(R.id.about_project_link);
        Pattern _projectUrlPattern = Pattern.compile(getString(R.string.about_project_link));

        // Linkify.TransformFilter() - String transformUrl(Matcher match, String url).
        Linkify.TransformFilter _projectUrlFilter = (Matcher match, String url) -> {
            return getString(R.string.about_project_url);
        };
        Linkify.addLinks(_projectView, _projectUrlPattern, getString(R.string.about_project_url), null, _projectUrlFilter);

        TextView _toiletMapView = (TextView) view.findViewById(R.id.about_credits_toiletmap_link);
        Pattern _toiletMapUrlPattern = Pattern.compile(getString(R.string.about_credits_toiletmap_link));
        Linkify.TransformFilter _toiletMapUrlFilter = (Matcher match, String url) -> {
            return getString(R.string.about_credits_toiletmap_url);
        };
        Linkify.addLinks(_toiletMapView, _toiletMapUrlPattern, getString(R.string.about_credits_toiletmap_url), null, _toiletMapUrlFilter);

        TextView _retrolambdaView = (TextView) view.findViewById(R.id.about_credits_retrolambda_link);
        Pattern _retrolambdaPattern = Pattern.compile(getString(R.string.about_credits_retrolambda_link));
        Linkify.TransformFilter _retrolambdaUrlFilter = (Matcher match, String url) -> {
            return getString(R.string.about_credits_retrolambda_url);
        };
        Linkify.addLinks(_retrolambdaView, _retrolambdaPattern, getString(R.string.about_credits_retrolambda_url), null, _retrolambdaUrlFilter);

        TextView _lightweightStreamApiView = (TextView) view.findViewById(R.id.about_credits_lightweight_stream_api_link);
        Pattern _lightweightStreamApiPattern = Pattern.compile(getString(R.string.about_credits_lightweight_stream_api_link));
        Linkify.TransformFilter _lightweightStreamApiUrlFilter = (Matcher match, String url) -> {
            return getString(R.string.about_credits_lightweight_stream_api_url);
        };
        Linkify.addLinks(_lightweightStreamApiView, _lightweightStreamApiPattern, getString(R.string.about_credits_lightweight_stream_api_url), null, _lightweightStreamApiUrlFilter);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            interactionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    public interface OnFragmentInteractionListener {
    }

}

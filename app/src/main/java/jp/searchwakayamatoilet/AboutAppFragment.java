package jp.searchwakayamatoilet;

import android.annotation.TargetApi;
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
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            // hide home button's title.
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // add links to textviews.
        TextView _projectView = (TextView) view.findViewById(R.id.about_project_link);
        Pattern _projectUrlPattern = Pattern.compile(getString(R.string.about_project_link));

        // Linkify.TransformFilter() - String transformUrl(Matcher match, String url).
        Linkify.TransformFilter _projectUrlFilter = (Matcher match, String url) -> {
            return getString(R.string.about_project_url);
        };
        Linkify.addLinks(_projectView, _projectUrlPattern, getString(R.string.about_project_url), null, _projectUrlFilter);

        setLink(view
                , R.id.about_credits_toiletmap_link
                , getString(R.string.about_credits_toiletmap_link)
                , getString(R.string.about_credits_toiletmap_url));
        setLink(view
                , R.id.about_credits_retrolambda_link
                , getString(R.string.about_credits_retrolambda_link)
                , getString(R.string.about_credits_retrolambda_url));
        setLink(view
                , R.id.about_credits_lightweight_stream_api_link
                , getString(R.string.about_credits_lightweight_stream_api_link)
                , getString(R.string.about_credits_lightweight_stream_api_url));
        setLink(view
                , R.id.about_credits_android_retrolambda_lombok_link
                , getString(R.string.about_credits_android_retrolambda_lombok_link)
                , getString(R.string.about_credits_android_retrolambda_lombok_url));
        // Inflate the layout for this fragment
        return view;
    }

    public interface OnFragmentInteractionListener {
    }
    private void setLink(View view, int intLinkId, String strLinkText, String strLinkUrl){
        TextView _textView = (TextView) view.findViewById(intLinkId);
        Pattern _pattern = Pattern.compile(strLinkText);
        Linkify.TransformFilter _urlFilter = (Matcher match, String url) -> {
            return strLinkUrl;
        };
        Linkify.addLinks(_textView, _pattern, strLinkUrl, null, _urlFilter);
    }
}

package jp.searchwakayamatoilet

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.util.Linkify
import android.transition.Fade
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.regex.Matcher
import java.util.regex.Pattern

class AboutAppFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setupWindowAnimations()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupWindowAnimations() {
        this.allowEnterTransitionOverlap = true
        val fade = Fade()
        fade.duration = 500
        // animation on returning by home button.
        this.exitTransition = fade
        // animation on returning by backkey of device.
        this.returnTransition = fade
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_about_app, container, false)
        val toolbar = view?.findViewById(R.id.toolbar) as Toolbar
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            // hide home button's title.
            actionBar.setDisplayShowTitleEnabled(false)
        }

        // add links to textviews.
        val _projectView = view?.findViewById(R.id.about_project_link) as TextView
        val _projectUrlPattern = Pattern.compile(getString(R.string.about_project_link))

        // Linkify.TransformFilter() - String transformUrl(Matcher match, String url).
        val _projectUrlFilter = { match: Matcher, url: String -> getString(R.string.about_project_url) }
        Linkify.addLinks(_projectView, _projectUrlPattern, getString(R.string.about_project_url), null, _projectUrlFilter)

        if(view != null){
            setLink(view, R.id.about_credits_toiletmap_link, getString(R.string.about_credits_toiletmap_link), getString(R.string.about_credits_toiletmap_url))
            setLink(view, R.id.about_credits_retrolambda_link, getString(R.string.about_credits_retrolambda_link), getString(R.string.about_credits_retrolambda_url))
            setLink(view, R.id.about_credits_lightweight_stream_api_link, getString(R.string.about_credits_lightweight_stream_api_link), getString(R.string.about_credits_lightweight_stream_api_url))
            setLink(view, R.id.about_credits_android_retrolambda_lombok_link, getString(R.string.about_credits_android_retrolambda_lombok_link), getString(R.string.about_credits_android_retrolambda_lombok_url))
        }
        // Inflate the layout for this fragment
        return view
    }

    interface OnFragmentInteractionListener

    private fun setLink(view: View, intLinkId: Int, strLinkText: String, strLinkUrl: String) {
        val _textView = view.findViewById(intLinkId) as TextView
        val _pattern = Pattern.compile(strLinkText)
        val _urlFilter = { match: Matcher, url: String -> strLinkUrl }
        Linkify.addLinks(_textView, _pattern, strLinkUrl, null, _urlFilter)
    }
}// Required empty public constructor

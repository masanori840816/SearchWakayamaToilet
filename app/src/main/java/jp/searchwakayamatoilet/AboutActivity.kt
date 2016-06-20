package jp.searchwakayamatoilet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.util.Linkify
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import java.util.regex.Matcher
import java.util.regex.Pattern

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        // hide home button's title.
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // add links to textviews.
        val projectView = findViewById(R.id.about_project_link) as TextView
        val projectUrlPattern = Pattern.compile(getString(R.string.about_project_link))

        // Linkify.TransformFilter() - String transformUrl(Matcher match, String url).
        val _projectUrlFilter = { match: Matcher, url: String -> getString(R.string.about_project_url) }
        Linkify.addLinks(projectView, projectUrlPattern, getString(R.string.about_project_url), null, _projectUrlFilter)

        setLink(R.id.about_credits_toiletmap_link, getString(R.string.about_credits_toiletmap_link), getString(R.string.about_credits_toiletmap_url))
        setLink(R.id.about_credits_retrolambda_link, getString(R.string.about_credits_retrolambda_link), getString(R.string.about_credits_retrolambda_url))
        setLink(R.id.about_credits_lightweight_stream_api_link, getString(R.string.about_credits_lightweight_stream_api_link), getString(R.string.about_credits_lightweight_stream_api_url))
        setLink(R.id.about_credits_android_retrolambda_lombok_link, getString(R.string.about_credits_android_retrolambda_lombok_link), getString(R.string.about_credits_android_retrolambda_lombok_url))

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->{
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setLink(intLinkId: Int, strLinkText: String, strLinkUrl: String) {
        val textView = findViewById(intLinkId) as TextView
        val pattern = Pattern.compile(strLinkText)
        val urlFilter = { match: Matcher, url: String -> strLinkUrl }
        Linkify.addLinks(textView, pattern, strLinkUrl, null, urlFilter)
    }
}

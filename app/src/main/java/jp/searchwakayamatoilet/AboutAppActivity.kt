/**
 * Created by Masanori on 2015/12/09.
 * this activity is for introducing this application page.
 */
package jp.searchwakayamatoilet

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.util.Linkify
import android.util.Log
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import jp.searchwakayamatoilet.databinding.ActivityAboutBinding
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

class AboutAppActivity : AppCompatActivity() {
    lateinit private var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        // hide home button's title.
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.aboutListview.scrollBarStyle = ListView.SCROLLBARS_OUTSIDE_OVERLAY;
        val aboutDataList: ArrayList<AboutListItem> = ArrayList()
        // 第二引数: 大項目(このアプリについてorCredit)のタイトルがある場合はTrue.
        // 第四引数: 小項目(Creditの各項目名)のタイトルがある場合はTrue.
        aboutDataList.add(addItem(getString(R.string.about_title)
                , true
                , ""
                , false
                , getString(R.string.about_description)
                , getString(R.string.about_project_url)))

        aboutDataList.add(addItem(getString(R.string.about_credits_title)
                , true
                , getString(R.string.about_credits_title_toiletmap)
                , true
                , getString(R.string.about_credits_toiletmap)
                , getString(R.string.about_credits_toiletmap_url)))

        aboutDataList.add(addItem(""
                , false
                , getString(R.string.about_credits_title_kotlin)
                , true
                , getString(R.string.about_credits_kotlin)
                , getString(R.string.about_credits_kotlin_url)))

        aboutDataList.add(addItem(""
                , false
                , getString(R.string.about_credits_title_rxjava)
                , true
                , getString(R.string.about_credits_rxjava)
                , getString(R.string.about_credits_rxjava_url)))

        aboutDataList.add(addItem(""
                , false
                , getString(R.string.about_credits_title_rxandroid)
                , true
                , getString(R.string.about_credits_rxandroid)
                , getString(R.string.about_credits_rxandroid_url)))

        aboutDataList.add(addItem(""
                , false
                , getString(R.string.about_credits_title_lightweightstreamapi)
                , true
                , getString(R.string.about_credits_lightweightstreamapi)
                , getString(R.string.about_credits_lightweightstreamapi_url)))

        val aboutDataAdapter: AboutDataAdapter = AboutDataAdapter(this, aboutDataList)
        binding.aboutListview.adapter = aboutDataAdapter
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
    private fun addItem(areaTitle: String, hasAreaTitle: Boolean, itemTitle: String, hasItemTitle: Boolean
                        , description: String, link: String): AboutListItem{
        val newItem = AboutListItem(areaTitle, hasAreaTitle, itemTitle, hasItemTitle
                , description, link)

        return newItem
    }
    private fun setLink(intLinkId: Int, strLinkText: String, strLinkUrl: String) {
        val textView = findViewById(intLinkId) as TextView
        val pattern = Pattern.compile(strLinkText)
        val urlFilter = { match: Matcher, url: String -> strLinkUrl }
        Linkify.addLinks(textView, pattern, strLinkUrl, null, urlFilter)
    }
}

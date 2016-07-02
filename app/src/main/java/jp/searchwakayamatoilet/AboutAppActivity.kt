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
    private var homeId = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)
        // ToolbarのHomeボタンを押した時のItemID.
        homeId = resources.getIdentifier("android:id/home", null, null)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        // hide home button's title.
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.aboutListview.scrollBarStyle = ListView.SCROLLBARS_OUTSIDE_OVERLAY;
        val aboutDataList: ArrayList<AboutAppListItem> = ArrayList()
        // 第二引数: 大項目(このアプリについてorCredit)のタイトルがある場合はTrue.
        // 第四引数: 小項目(Creditの各項目名)のタイトルがある場合はTrue.
        aboutDataList.add(getItem(getString(R.string.about_title)
                , true
                , ""
                , false
                , getString(R.string.about_description)
                , getString(R.string.about_project_url)))

        aboutDataList.add(getItem(getString(R.string.about_credits_title)
                , true
                , getString(R.string.about_credits_title_toiletmap)
                , true
                , getString(R.string.about_credits_toiletmap)
                , getString(R.string.about_credits_toiletmap_url)))

        aboutDataList.add(getItem(""
                , false
                , getString(R.string.about_credits_title_kotlin)
                , true
                , getString(R.string.about_credits_kotlin)
                , getString(R.string.about_credits_kotlin_url)))

        aboutDataList.add(getItem(""
                , false
                , getString(R.string.about_credits_title_rxjava)
                , true
                , getString(R.string.about_credits_rxjava)
                , getString(R.string.about_credits_rxjava_url)))

        aboutDataList.add(getItem(""
                , false
                , getString(R.string.about_credits_title_rxandroid)
                , true
                , getString(R.string.about_credits_rxandroid)
                , getString(R.string.about_credits_rxandroid_url)))

        aboutDataList.add(getItem(""
                , false
                , getString(R.string.about_credits_title_lightweightstreamapi)
                , true
                , getString(R.string.about_credits_lightweightstreamapi)
                , getString(R.string.about_credits_lightweightstreamapi_url)))

        binding.aboutListview.adapter = AboutAppDataAdapter(this, aboutDataList)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            homeId ->{
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun getItem(areaTitle: String, hasAreaTitle: Boolean, itemTitle: String, hasItemTitle: Boolean
                        , description: String, link: String): AboutAppListItem {
        val newItem = AboutAppListItem(areaTitle, hasAreaTitle, itemTitle, hasItemTitle
                , description, link)

        return newItem
    }
}

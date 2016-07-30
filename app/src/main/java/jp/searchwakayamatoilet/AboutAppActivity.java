package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/07/30.
 * about this application page activity.
 */
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import jp.searchwakayamatoilet.databinding.ActivityAboutappBinding;
import java.util.ArrayList;

public class AboutAppActivity extends AppCompatActivity{
    private int homeId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAboutappBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_aboutapp);
        // ToolbarのHomeボタンを押した時のItemID.
        homeId = getResources().getIdentifier("android:id/home", null, null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        // hide home button's title.
        actionBar.setDisplayShowTitleEnabled(false);

        binding.aboutListview.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        ArrayList<AboutAppListItem> aboutDataList = new ArrayList<>();
        // 第二引数: 大項目(このアプリについてorCredit)のタイトルがある場合はTrue.
        // 第四引数: 小項目(Creditの各項目名)のタイトルがある場合はTrue.
        aboutDataList.add(getItem(getString(R.string.about_title)
                , true
                , ""
                , false
                , getString(R.string.about_description)
                , getString(R.string.about_project_url)));

        aboutDataList.add(getItem(getString(R.string.about_credits_title)
                , true
                , getString(R.string.about_credits_title_toiletmap)
                , true
                , getString(R.string.about_credits_toiletmap)
                , getString(R.string.about_credits_toiletmap_url)));

        aboutDataList.add(getItem(""
                , false
                , getString(R.string.about_credits_title_kotlin)
                , true
                , getString(R.string.about_credits_kotlin)
                , getString(R.string.about_credits_kotlin_url)));

        aboutDataList.add(getItem(""
                , false
                , getString(R.string.about_credits_title_rxjava)
                , true
                , getString(R.string.about_credits_rxjava)
                , getString(R.string.about_credits_rxjava_url)));

        aboutDataList.add(getItem(""
                , false
                , getString(R.string.about_credits_title_rxandroid)
                , true
                , getString(R.string.about_credits_rxandroid)
                , getString(R.string.about_credits_rxandroid_url)));

        binding.aboutListview.setAdapter(new AboutAppDataAdapter(this, aboutDataList));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == homeId) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private AboutAppListItem getItem(@NonNull String areaTitle, boolean hasAreaTitle, @NonNull String itemTitle
            , boolean hasItemTitle, @NonNull String description, @NonNull String link){
        return new AboutAppListItem(areaTitle, hasAreaTitle, itemTitle
                , hasItemTitle, description, link);
    }
}

package jp.searchwakayamatoilet

import android.content.Context
import android.databinding.DataBindingUtil
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.searchwakayamatoilet.databinding.AboutAppListitemBinding
import java.util.ArrayList

/**
 * Created by masanori on 2016/06/21.
 */
class AboutAppDataAdapter(context: Context, newListItemList: ArrayList<AboutAppListItem>) : BaseAdapter() {
    lateinit private var layoutInflater :LayoutInflater
    lateinit private var aboutDataList: ArrayList<AboutAppListItem>
    init{
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        aboutDataList = newListItemList
    }
    override fun getCount(): Int{
        return aboutDataList.count()
    }
    override fun getItem(position: Int): AboutAppListItem {
        return aboutDataList.get(position)
    }
    override fun getItemId(position: Int): Long{
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val binding: AboutAppListitemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.about_app_listitem, parent, false)

        binding.aboutappitemclass = aboutDataList.get(position)

        return binding.root
    }
}
package jp.searchwakayamatoilet

import android.content.Context
import android.databinding.DataBindingUtil
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import jp.searchwakayamatoilet.databinding.AboutListItemBinding
import java.util.ArrayList

/**
 * Created by masanori on 2016/06/21.
 */
class AboutDataAdapter(context: Context, newListItemList: ArrayList<AboutListItem>) : BaseAdapter() {
    lateinit private var currentContext: Context
    lateinit private var layoutInflater :LayoutInflater
    lateinit private var aboutDataList: ArrayList<AboutListItem>
    init{
        currentContext = context
        layoutInflater = currentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        aboutDataList = newListItemList
    }
    override fun getCount(): Int{
        return aboutDataList.count()
    }
    override fun getItem(position: Int): AboutListItem{
        return aboutDataList.get(position)
    }
    override fun getItemId(position: Int): Long{
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        if(convertView == null){
            val binding: AboutListItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.about_list_item, parent, false)

            binding.aboutappitemclass = aboutDataList.get(position)

            return binding.root
        }
        return convertView
    }
}
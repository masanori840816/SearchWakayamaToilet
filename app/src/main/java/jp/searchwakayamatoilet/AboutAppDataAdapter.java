package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/06/21.
 * ListView's adapter class for AboutAppActivity.
 */

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import jp.searchwakayamatoilet.databinding.AboutAppListitemBinding;
import java.util.ArrayList;

public class AboutAppDataAdapter extends BaseAdapter{
    @NonNull
    private LayoutInflater layoutInflater;
    @NonNull
    private ArrayList<AboutAppListItem> aboutDataList;

    public AboutAppDataAdapter(Context context, @NonNull ArrayList<AboutAppListItem> newListItemList){
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        aboutDataList = newListItemList;
    }
    @Override
    public int getCount(){
        return aboutDataList.size();
    }
    @Override
    public AboutAppListItem getItem(int position){
        return aboutDataList.get(position);
    }
    @Override
    public long getItemId(int position){
        return (long)position;
    }
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        AboutAppListitemBinding binding;

        if(convertView == null){
            binding = DataBindingUtil.inflate(layoutInflater, R.layout.about_app_listitem, parent, false);
            if(binding != null){
                convertView = binding.getRoot();
                convertView.setTag(binding);
            }
        }
        else{
            binding = (AboutAppListitemBinding) convertView.getTag();
        }

        if(binding == null){
            return null;
        }
        binding.setAboutappitemclass(aboutDataList.get(position));
        return binding.getRoot();
    }
}

package jp.searchwakayamatoilet;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import jp.searchwakayamatoilet.databinding.LayoutSuggestItemBinding;

/**
 * Created by masanori on 2016/09/17.
 * ListView's adapter class for Suggestions.
 */
public class SuggestDataAdapter extends BaseAdapter {
    @NonNull
    private LayoutInflater layoutInflater;
    @NonNull
    private ArrayList<SuggestListItem> suggestList;

    public SuggestDataAdapter(Context context, @NonNull ArrayList<SuggestListItem> newItemList){
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        suggestList = newItemList;
    }
    @Override
    public int getCount(){
        return suggestList.size();
    }
    @Override
    public SuggestListItem getItem(int position){
        return suggestList.get(position);
    }
    @Override
    public long getItemId(int position){
        return (long)position;
    }
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutSuggestItemBinding binding;
        if(convertView == null) {
            binding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_suggest_item, parent, false);
            if (binding != null) {
                convertView = binding.getRoot();
                convertView.setTag(binding);
            }
        }
        else{
            binding = (LayoutSuggestItemBinding) convertView.getTag();
        }
        if(binding == null){
            return null;
        }
        binding.setSuggestitemclass(suggestList.get(position));
        return binding.getRoot();
    }
}

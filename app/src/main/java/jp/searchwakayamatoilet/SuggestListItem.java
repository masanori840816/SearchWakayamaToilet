package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/09/16.
 * this is list item view of suggestion.
 */
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

public class SuggestListItem extends BaseObservable {
    @NonNull
    private String toiletName;
    @NonNull
    private String address;
    public SuggestListItem(@NonNull String setToiletName, @NonNull String setAddress){
        toiletName = setToiletName;
        address = setAddress;
    }
    @Bindable
    public String getToiletName(){
        return toiletName;
    }
    @Bindable
    public String getAddress(){
        return address;
    }
}

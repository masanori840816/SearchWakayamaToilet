package jp.searchwakayamatoilet;

import android.support.annotation.NonNull;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by masanori on 2016/07/31.
 * List view item class.
 */
public class AboutAppListItem extends BaseObservable{
    private String areaTitle = "";
    private boolean hasAreaTitle = false;
    private String itemTitle = "";
    private boolean hasItemTitle = false;
    private String description = "";
    private String link = "";

    public AboutAppListItem(@NonNull String newAreaTitle, boolean newHasAreaTitle, @NonNull String newItemTitle
            , boolean newHasItemTitle, @NonNull String newDescription, @NonNull String newLink){
        areaTitle = newAreaTitle;
        hasAreaTitle = newHasAreaTitle;
        itemTitle = newItemTitle;
        hasItemTitle = newHasItemTitle;
        description = newDescription;
        link = newLink;
    }
    @Bindable
    public String getAreaTitle(){
        return areaTitle;
    }
    @Bindable
    public boolean getHasAreaTitle(){
        return hasAreaTitle;
    }
    @Bindable
    public String getItemTitle(){
        return itemTitle;
    }
    @Bindable
    public boolean getHasItemTitle(){
        return hasItemTitle;
    }
    @Bindable
    public String getDescription(){
        return description;
    }
    @Bindable
    public String getLink(){
        return link;
    }
}

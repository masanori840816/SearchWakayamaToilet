package jp.searchwakayamatoilet

import android.databinding.BaseObservable
import android.databinding.Bindable

/**
 * Created by masanori on 2016/06/21.
 */
class AboutAppListItem(newAreaTitle: String, newHasAreaTitle: Boolean
                       , newItemTitle: String, newHasItemTitle: Boolean, newDescription: String, newLink: String): BaseObservable(){
    private var areaTitle = ""
    private var hasAreaTitle = false
    private var itemTitle = ""
    private var hasItemTitle = false
    private var description = ""
    private var link = ""

    init{
        areaTitle = newAreaTitle
        hasAreaTitle = newHasAreaTitle
        itemTitle = newItemTitle
        hasItemTitle = newHasItemTitle
        description = newDescription
        link = newLink
    }
    @Bindable fun getAreaTitle(): String{
        return areaTitle
    }
    @Bindable fun getHasAreaTitle(): Boolean{
        return hasAreaTitle
    }
    @Bindable fun getItemTitle(): String{
        return itemTitle
    }
    @Bindable fun getHasItemTitle(): Boolean{
        return hasItemTitle
    }
    @Bindable fun getDescription(): String{
        return description
    }
    @Bindable fun getLink(): String{
        return link
    }
}
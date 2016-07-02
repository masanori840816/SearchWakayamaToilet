package jp.searchwakayamatoilet

/**
 * Created by masanori on 2016/07/02.
 */
class LoadingPanelEvent(willLoadingCsvBeStopped: Boolean) {
    private var isLoadingCsvStopped: Boolean
    init{
        isLoadingCsvStopped = willLoadingCsvBeStopped
    }
    fun getIsLoadingCsvStopped(): Boolean{
        return isLoadingCsvStopped
    }
}
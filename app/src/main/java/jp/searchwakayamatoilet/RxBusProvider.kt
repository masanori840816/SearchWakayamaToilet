package jp.searchwakayamatoilet

/**
 * Created by masanori on 2016/06/26.
 * this class is provider of RxBus.
 */


class RxBusProvider {
    companion object{
        private val Bus: RxBus = RxBus()

        fun getInstance(): RxBus{
            return Bus
        }
    }

}
package jp.searchwakayamatoilet

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.AsyncTask
import android.util.Log

import com.annimon.stream.Stream

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.regex.Pattern

/**
 * Created by masanori on 2016/01/24.
 */
class ToiletDataLoader(private val currentActivity: Activity, private val isExistingDataUsed: Boolean
                       , private val currentPresenter: MainPresenter, private var strQuery: String?) : AsyncTask<Void, Void, Int>() {

    private val dbAccesser: DatabaseAccesser
    private val sqlite: SQLiteDatabase
    private var isTransactionStarted: Boolean = false
    private var isLoadingCancelled: Boolean = false

    init {
        dbAccesser = DatabaseAccesser(currentActivity as Context)
        sqlite = dbAccesser.writableDatabase
        isLoadingCancelled = false
    }

    fun stopLoading() {
        isLoadingCancelled = true
    }


    override fun doInBackground(vararg params: Void): Int? {
        dbAccesser.setSearchCriteriaFromFreeWord(strQuery)
        val aryToiletInfo = dbAccesser.search(sqlite)
        isTransactionStarted = false

        if (isExistingDataUsed
                && aryToiletInfo.size > 0) {
            // Add marker on UI Thread.
            currentPresenter.addMarker(aryToiletInfo)
        } else {
            // Runnable() - run().
            currentPresenter.startLoadingCsvData()

            val asmAsset = currentActivity.resources?.assets
            try {
                // CSVの読み込み.
                val ipsInput = asmAsset?.open("toilet-map-edited.csv")
                val inputStreamReader = InputStreamReader(ipsInput)
                val bufferReader = BufferedReader(inputStreamReader)
                var strLine: String? = ""
                var strSplited: Array<String>?
                val patternNum = Pattern.compile("^[0-9]+")

                val aryLoadedToiletInfo = ArrayList<DatabaseAccesser.ToiletInfoModel>()
                // 1行ずつ読み込む.
                while (true) {
                    if (isLoadingCancelled) {
                        if (isTransactionStarted) {
                            // CommitしてTransactionを終了.
                            sqlite.setTransactionSuccessful()
                            sqlite.endTransaction()
                        }
                        break
                    }

                    strLine = bufferReader.readLine()
                    if (strLine == null) {
                        // stop loading if the line is empty.
                        break
                    }

                    // if the line isn't start with number, skip loading.
                    if (!patternNum.matcher(strLine).find()) {
                        continue
                    }
                    // Split後に8件データがある行のみ.
                    strSplited = strLine.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    if (strSplited.size >= 8) {
                        // 0:No, 1:名称, 2:市町村名, 3:住所, 4: 緯度, 5:経度, 6:利用時間, 7: 多目的トイレの有無.
                        var toiletInfoModel: DatabaseAccesser.ToiletInfoModel? = DatabaseAccesser.ToiletInfoModel(
                                toiletName = strSplited[1]
                                // 都道府県は和歌山固定.
                                , district = "和歌山"
                                , municipality = strSplited[2]
                                , address = strSplited[3]
                                , latitude = tryParseToDouble(strSplited[4])
                                , longitude = tryParseToDouble(strSplited[5])
                                , availableTime = strSplited[6]
                                , hasMultiPurposeToilet = java.lang.Boolean.valueOf(strSplited[7]))

                        if(toiletInfoModel != null){
                            aryLoadedToiletInfo.add(toiletInfoModel)

                            val _toiletName = toiletInfoModel.toiletName
                            val _toiletAddress = toiletInfoModel.address
                            val lngCount = Stream.of(aryToiletInfo).filter {
                                    toiletInfo ->
                                        toiletInfo.toiletName == _toiletName && toiletInfo.address == _toiletAddress
                                }.count()

                            if (lngCount <= 0) {
                                if (isTransactionStarted) {
                                    // Transactionの開始.
                                    sqlite.beginTransaction()
                                    isTransactionStarted = true
                                }

                                // if there is no same data, insert as new one.
                                dbAccesser.insertInfo(sqlite, toiletInfoModel)
                            }
                            toiletInfoModel = null
                        }
                    }
                }
                if (isTransactionStarted) {
                    // CommitしてTransactionを終了.
                    sqlite.setTransactionSuccessful()
                    sqlite.endTransaction()
                }

                bufferReader.close()
                ipsInput?.close()

                // Add marker on UI Thread.
                currentPresenter.addMarker(aryLoadedToiletInfo)

                currentPresenter.stopLoadingCsvData()

            } catch (ex: IOException) {
                currentPresenter.showErrorDialog(ex.message)
            } catch (ex: SQLiteException) {
                currentPresenter.showErrorDialog(ex.message)
            }

        }
        return 0
    }

    private fun tryParseToDouble(numberString: String): Double {
        try {
            val numberDouble = java.lang.Double.parseDouble(numberString)
            return numberDouble
        } catch (e: NumberFormatException) {
            return 0.0
        }

    }

    override fun onPostExecute(result: Int?) {
    }

    override fun onCancelled() {
        if (isTransactionStarted) {
            sqlite.setTransactionSuccessful()
            sqlite.endTransaction()
        }
    }
}

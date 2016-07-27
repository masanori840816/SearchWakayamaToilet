package jp.searchwakayamatoilet

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import rx.Observable

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.regex.Pattern

import jp.searchwakayamatoilet.DatabaseAccesser.ToiletInfoClass

/**
 * Created by masanori on 2016/01/24.
 */
class ToiletDataLoader(val currentActivity: Activity) {

    private val dbAccesser: DatabaseAccesser
    private val sqlite: SQLiteDatabase
    private var isTransactionStarted: Boolean = false
    private var isLoadingCancelled: Boolean = false

    init {
        dbAccesser = DatabaseAccesser(currentActivity as Context)
        sqlite = dbAccesser.writableDatabase
    }

    fun loadToiletData(isExistingDataUsed: Boolean, strQuery: String?): Observable<ArrayList<ToiletInfoClass>> {
        val loadToiletDataObservable: Observable<ArrayList<ToiletInfoClass>> = Observable.create {
            // subscriber: Subscriber<in ToiletInfoModel>
            subscriber ->
            run {
                dbAccesser.setSearchCriteriaFromFreeWord(strQuery)
                val toiletInfoList = dbAccesser.search(sqlite)
                isTransactionStarted = false

                if (isExistingDataUsed
                        && toiletInfoList.size > 0) {
                    // Add marker on UI Thread.
                    subscriber.onNext(toiletInfoList)
                    subscriber.onCompleted()
                } else {

                    val asmAsset = currentActivity.resources?.assets
                    try {
                        // CSVの読み込み.
                        val ipsInput = asmAsset?.open("toilet-map-edited.csv")
                        val inputStreamReader = InputStreamReader(ipsInput)
                        val bufferReader = BufferedReader(inputStreamReader)
                        val patternNum = Pattern.compile("^[0-9]+")

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

                            val strLine = bufferReader.readLine()
                            strLine ?: break

                            // if the line isn't start with number, skip loading.
                            if (!patternNum.matcher(strLine).find()) {
                                continue
                            }
                            // Split後に8件データがある行のみ.
                            val strSplited = strLine.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                            if (strSplited.size >= 8) {
                                // 0:No, 1:名称, 2:市町村名, 3:住所, 4: 緯度, 5:経度, 6:利用時間, 7: 多目的トイレの有無.
                                var toiletInfoModel: ToiletInfoClass? = DatabaseAccesser.ToiletInfoClass(
                                        toiletName = strSplited[1]
                                        // 都道府県は和歌山固定.
                                        , district = "和歌山"
                                        , municipality = strSplited[2]
                                        , address = strSplited[3]
                                        , latitude = tryParseToDouble(strSplited[4])
                                        , longitude = tryParseToDouble(strSplited[5])
                                        , availableTime = strSplited[6]
                                        , hasMultiPurposeToilet = java.lang.Boolean.valueOf(strSplited[7]))

                                if (toiletInfoModel != null) {
                                    toiletInfoList.add(toiletInfoModel)

                                    val toiletName = toiletInfoModel.toiletName
                                    val _toiletAddress = toiletInfoModel.address
                                    val lngCount = toiletInfoList.filter {
                                        toiletInfo ->
                                        toiletInfo.toiletName == toiletName
                                                && toiletInfo.address == _toiletAddress
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
                        subscriber.onNext(toiletInfoList)
                        subscriber.onCompleted()

                    } catch (ex: IOException) {
                        throw Exception(ex)
                    } catch (ex: SQLiteException) {
                        throw Exception(ex)
                    }

                }
            }
        }
        return loadToiletDataObservable
    }
    fun searchToiletData(strQuery: String?): Observable<ArrayList<ToiletInfoClass>> {
        val searchToiletDataObservable: Observable<ArrayList<ToiletInfoClass>> = Observable.create {
            // subscriber: Subscriber<in ToiletInfoModel>
            subscriber ->
            run {
                dbAccesser.setSearchCriteriaFromFreeWord(strQuery)

                // Add marker on UI Thread.
                subscriber.onNext(dbAccesser.search(dbAccesser.writableDatabase))
                subscriber.onCompleted()
            }
        }
        return searchToiletDataObservable
    }

    fun stopLoading() {
        isLoadingCancelled = true
    }
    private fun tryParseToDouble(numberString: String): Double {
        try {
            val numberDouble = java.lang.Double.parseDouble(numberString)
            return numberDouble
        } catch (e: NumberFormatException) {
            return 0.0
        }

    }

    fun onCancelled() {
        if (isTransactionStarted) {
            sqlite.setTransactionSuccessful()
            sqlite.endTransaction()
        }
    }
}

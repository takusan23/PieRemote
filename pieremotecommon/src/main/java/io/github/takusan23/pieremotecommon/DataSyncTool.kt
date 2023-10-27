package io.github.takusan23.pieremotecommon

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import io.github.takusan23.pieremotecommon.data.MusicItem
import io.github.takusan23.pieremotecommon.data.PieRemoteData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** 音楽アプリのキューを同期する */
object DataSyncTool {

    /** 同期アイテムのパス？ */
    private const val PATH = "/pieremote_datasync"

    private const val ITEM_QUEUE = "ITEM_QUEUE"
    private const val CURRENT_PLAYING_ITEM = "CURRENT_PLAYING_ITEM"
    private const val IS_PLAYING = "IS_PLAYING"

    /**
     * 同期するデータを入れる
     *
     * @param context [Context]
     * @param text テキスト
     */
    suspend fun sendPieRemoteData(context: Context, pieRemoteData: PieRemoteData) =
        sendDataSync(
            context = context,
            keyValue = mapOf(
                CURRENT_PLAYING_ITEM to MusicItem.toJson(pieRemoteData.currentPlayingMusicItem),
                ITEM_QUEUE to MusicItem.toJsonList(pieRemoteData.itemQueue),
                IS_PLAYING to pieRemoteData.isPlaying
            )
        )

    /**
     * 同期しているデータを受け取る
     *
     * @param context [Context]
     */
    fun receivePieRemoteData(context: Context) =
        receiveDataSync(context).map { dataMap ->
            // データクラスにして返す
            val currentPlayingMusicItem = dataMap.getString(CURRENT_PLAYING_ITEM)!!
                .let { json -> MusicItem.toItem(json) }
            val itemQueue = dataMap.getString(ITEM_QUEUE)!!
                .let { json -> MusicItem.toItemList(json) }
            val isPlaying = dataMap.getBoolean(IS_PLAYING)
            // キーを使って値を取り出す
            PieRemoteData(
                isPlaying = isPlaying,
                currentPlayingMusicItem = currentPlayingMusicItem,
                itemQueue = itemQueue
            )
        }


    /** 同期するデータを設定する */
    private suspend fun sendDataSync(context: Context, keyValue: Map<String, Any>) {
        // Bundle みたいな感じ
        val request = PutDataMapRequest.create(PATH).apply {
            // キーと値
            keyValue.forEach { entry ->
                when (val entryValue = entry.value) {
                    is String -> dataMap.putString(entry.key, entryValue)
                    is Boolean -> dataMap.putBoolean(entry.key, entryValue)
                }
            }
        }.asPutDataRequest()
        Wearable.getDataClient(context).putDataItem(request).await()
    }

    /** 同期するデータを受け取る */
    private fun receiveDataSync(context: Context) = callbackFlow {

        fun sendResult(dataItem: DataItem) {
            when (dataItem.uri.path) {
                PATH -> {
                    // PutDataMapRequest を復元する
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    trySend(dataMap)
                }
            }
        }

        val listener = DataClient.OnDataChangedListener { dataEvents ->
            dataEvents
                .filter { it.type == DataEvent.TYPE_CHANGED }
                .forEach { event -> sendResult(event.dataItem) }
        }

        // 同期的に一回目は取得
        Wearable.getDataClient(context).dataItems.await()
            .let { buffer -> (0 until buffer.count).map { buffer.get(it) } }
            .forEach { dataItem -> sendResult(dataItem) }

        // アプリ起動中に変化した場合
        Wearable.getDataClient(context).addListener(listener)
        awaitClose { Wearable.getDataClient(context).removeListener(listener) }
    }
}
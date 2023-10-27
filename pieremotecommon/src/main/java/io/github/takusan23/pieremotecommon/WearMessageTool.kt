package io.github.takusan23.pieremotecommon

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import io.github.takusan23.pieremotecommon.data.MusicControlEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

object WearMessageTool {

    private const val PATH = "/pieremote_message"

    /**
     * 音楽操作のイベントを送る
     *
     * @param context [Context]
     * @param musicControlEvent [MusicControlEvent]
     */
    suspend fun sendMusicControlEvent(
        context: Context,
        musicControlEvent: MusicControlEvent
    ) = sendMessage(context, MusicControlEvent.toJson(musicControlEvent))

    /**
     * 音楽操作のイベントを受け取る
     *
     * @param context [Context]
     */
    fun receiveMusicControlEvent(context: Context) =
        receiveMessage(context)
            .map { json -> MusicControlEvent.toData(json) }

    /**
     * メッセージを送る
     * 全部のデバイスに送る
     *
     * @param context [Context]
     * @return 失敗したら例外
     */
    private suspend fun sendMessage(
        context: Context,
        text: String
    ) {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.forEach { node ->
            Wearable.getMessageClient(context).sendMessage(
                node.id,
                PATH,
                text.toByteArray(charset = Charsets.UTF_8)
            ).await()
        }
    }

    /**
     * メッセージを Flow で受信する
     *
     * @param context [Context]
     */
    private fun receiveMessage(context: Context) = callbackFlow {
        val client = Wearable.getMessageClient(context)
        val listener = MessageClient.OnMessageReceivedListener {
            trySend(it.data.toString(charset = Charsets.UTF_8))
        }

        client.addListener(listener)
        awaitClose { client.removeListener(listener) }
    }

}
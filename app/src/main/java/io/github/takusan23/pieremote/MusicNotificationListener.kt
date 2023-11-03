package io.github.takusan23.pieremote

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.takusan23.pieremotecommon.DataSyncTool
import io.github.takusan23.pieremotecommon.WearMessageTool
import io.github.takusan23.pieremotecommon.data.MusicControlEvent
import io.github.takusan23.pieremotecommon.data.MusicItem
import io.github.takusan23.pieremotecommon.data.PieRemoteData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

class MusicNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    /** 初期化 */
    override fun onCreate() {
        super.onCreate()

        scope.launch { setup() }
    }

    private suspend fun setup() {

        // 設定値を監視
        // クイック設定から有効/無効にできるので
        dataStore.data
            // 新しい値が来たらブロックが再起動
            .collectLatest {
                val isServiceEnable = it[DataStore.IS_SERVICE_ENABLE] == true
                // 使えない時
                if (!isServiceEnable) return@collectLatest

                // MediaController を取る
                // 新しい値が来たらブロックが再起動
                collectMediaController().collectLatest { controller ->

                    // collectLatest は CoroutineScope ではない
                    coroutineScope {

                        launch {
                            // WearOS からの命令に対応する
                            WearMessageTool.receiveMusicControlEvent(this@MusicNotificationListener).collect { musicControlEvent ->
                                when (musicControlEvent) {
                                    is MusicControlEvent.Play -> controller.transportControls.play()
                                    is MusicControlEvent.Pause -> controller.transportControls.pause()
                                    is MusicControlEvent.Next -> controller.transportControls.skipToNext()
                                    is MusicControlEvent.Prev -> controller.transportControls.skipToPrevious()
                                    is MusicControlEvent.MoveIndex -> controller.transportControls.skipToQueueItem(musicControlEvent.mediaId)
                                }
                            }

                        }

                        // WearOS へプレイヤーの状態を送る
                        launch {
                            coroutineScope {
                                collectPlayerChanged(controller).collect { pieRemoteData ->
                                    DataSyncTool.sendPieRemoteData(
                                        context = this@MusicNotificationListener,
                                        pieRemoteData = pieRemoteData
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }

    /** リソース開放 */
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    /** よその音楽アプリを操作するためのコントローラーを取得する */
    private fun collectMediaController() = callbackFlow {
        val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val componentName = ComponentName(this@MusicNotificationListener, MusicNotificationListener::class.java)

        fun sendResult() {
            mediaSessionManager.getActiveSessions(componentName).firstOrNull()?.sessionToken?.also { sessionToken ->
                trySend(MediaControllerCompat(this@MusicNotificationListener, MediaSessionCompat.Token.fromToken(sessionToken)))
            }
        }
        // 最初に一回送る
        sendResult()

        val callback = MediaSessionManager.OnActiveSessionsChangedListener { sendResult() }
        mediaSessionManager.addOnActiveSessionsChangedListener(callback, ComponentName(this@MusicNotificationListener, MusicNotificationListener::class.java))
        awaitClose { mediaSessionManager.removeOnActiveSessionsChangedListener(callback) }
    }

    /** MediaSession の状態変化コールバックを購読する */
    private fun collectPlayerChanged(controller: MediaControllerCompat) = callbackFlow {

        fun sendResult() {
            val currentPlayingItem = controller.queue
                ?.firstOrNull { it.description.title == controller.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) }
                ?.toMusicItem()
            val queueItem = controller.queue?.map { it.toMusicItem() } ?: emptyList()
            val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING

            if (currentPlayingItem != null) {
                trySend(
                    PieRemoteData(
                        isPlaying = isPlaying,
                        currentPlayingMusicItem = currentPlayingItem,
                        itemQueue = queueItem
                    )
                )
            }
        }
        // 最初に一回送る
        sendResult()

        val callback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)
                sendResult()
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                super.onMetadataChanged(metadata)
                sendResult()
            }

            override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
                super.onQueueChanged(queue)
                sendResult()
            }
        }
        controller.registerCallback(callback)
        awaitClose { controller.unregisterCallback(callback) }
    }

    /** MediaSession のを独自の [MusicItem] にする */
    private fun MediaSessionCompat.QueueItem.toMusicItem(): MusicItem = MusicItem(
        queueId = queueId,
        title = description.title.toString(),
        artist = description.subtitle.toString()
    )

}
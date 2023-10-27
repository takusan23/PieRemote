package io.github.takusan23.pieremote

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.github.takusan23.pieremotecommon.DataSyncTool
import io.github.takusan23.pieremotecommon.data.MusicControlEvent
import io.github.takusan23.pieremotecommon.WearMessageTool
import io.github.takusan23.pieremotecommon.data.MusicItem
import io.github.takusan23.pieremotecommon.data.PieRemoteData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MusicNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    /** よその音楽アプリと連携するために MediaSession を利用する */
    private val mediaSession by lazy {
        // MediaSession取得
        val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val activeMediaSessionList = mediaSessionManager.getActiveSessions(ComponentName(this, MusicNotificationListener::class.java))
        activeMediaSessionList.firstOrNull()
    }

    /** 初期化 */
    override fun onCreate() {
        super.onCreate()

        val mediaSession = mediaSession ?: return

        // WearOS から来るイベントが Flow で通知されるので監視する
        scope.launch {
            WearMessageTool.receiveMusicControlEvent(this@MusicNotificationListener).collect { musicControlEvent ->
                when (musicControlEvent) {
                    is MusicControlEvent.Play -> mediaSession.transportControls.play()
                    is MusicControlEvent.Pause -> mediaSession.transportControls.pause()
                    is MusicControlEvent.Next -> mediaSession.transportControls.skipToNext()
                    is MusicControlEvent.Prev -> mediaSession.transportControls.skipToPrevious()
                    is MusicControlEvent.MoveIndex -> mediaSession.transportControls.skipToQueueItem(musicControlEvent.mediaId)
                }
            }
        }
    }

    /** リソース開放 */
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    /** 通知更新時 */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val mediaSession = mediaSession ?: return

        // 音楽アプリの状態を WearOS の PieRemote へ送る
        scope.launch {
            val currentPlayingItem = mediaSession.queue
                ?.first { it.description.title == mediaSession.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) }
                ?.toMusicItem() ?: return@launch
            val queueItem = mediaSession.queue?.map { it.toMusicItem() } ?: emptyList()
            val isPlaying = mediaSession.playbackState?.state == PlaybackState.STATE_PLAYING

            DataSyncTool.sendPieRemoteData(
                context = this@MusicNotificationListener,
                pieRemoteData = PieRemoteData(
                    isPlaying = isPlaying,
                    currentPlayingMusicItem = currentPlayingItem,
                    itemQueue = queueItem
                )
            )
        }
    }

    /** MediaSession のを独自の [MusicItem] にする */
    private fun MediaSession.QueueItem.toMusicItem(): MusicItem = MusicItem(
        queueId = queueId,
        title = description.title.toString(),
        artist = description.subtitle.toString()
    )

}
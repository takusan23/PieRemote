package io.github.takusan23.pieremote

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController.Callback
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaControllerCompat.TransportControls
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
import kotlinx.coroutines.launch

class MusicNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    /** よその音楽アプリと連携するために MediaSession を利用する */
    private val mediaController: MediaControllerCompat?
        get() {
            // MediaSession取得
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val activeMediaSessionList = mediaSessionManager.getActiveSessions(ComponentName(this, MusicNotificationListener::class.java))
            // MediaSession から Compat 版の MediaSessionCompat にする
            val mediaSessionToken = activeMediaSessionList.firstOrNull()?.sessionToken ?: return null
            return MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(mediaSessionToken))
        }

    /** 初期化 */
    override fun onCreate() {
        super.onCreate()
        // WearOS から来るイベントが Flow で通知されるので監視する
        scope.launch {
            WearMessageTool.receiveMusicControlEvent(this@MusicNotificationListener).collect { musicControlEvent ->
                val mediaController = mediaController ?: return@collect

                when (musicControlEvent) {
                    is MusicControlEvent.Play -> mediaController.transportControls.play()
                    is MusicControlEvent.Pause -> mediaController.transportControls.pause()
                    is MusicControlEvent.Next -> mediaController.transportControls.skipToNext()
                    is MusicControlEvent.Prev -> mediaController.transportControls.skipToPrevious()
                    is MusicControlEvent.MoveIndex -> mediaController.transportControls.skipToQueueItem(musicControlEvent.mediaId)
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

        val mediaController = mediaController ?: return

        // 音楽アプリの状態を WearOS の PieRemote へ送る
        scope.launch {
            val currentPlayingItem = mediaController.queue
                ?.first { it.description.title == mediaController.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) }
                ?.toMusicItem() ?: return@launch
            val queueItem = mediaController.queue?.map { it.toMusicItem() } ?: emptyList()
            val isPlaying = mediaController.playbackState?.state == PlaybackState.STATE_PLAYING

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
    private fun MediaSessionCompat.QueueItem.toMusicItem(): MusicItem = MusicItem(
        queueId = queueId,
        title = description.title.toString(),
        artist = description.subtitle.toString()
    )

}
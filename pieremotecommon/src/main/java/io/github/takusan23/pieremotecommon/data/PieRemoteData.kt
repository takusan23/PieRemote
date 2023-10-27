package io.github.takusan23.pieremotecommon.data

/**
 * WearOS - Android でやり取りする同期するデータ
 *
 * @param currentPlayingMusicItem 再生中の音楽
 * @param itemQueue 再生キュー
 */
data class PieRemoteData(
    val isPlaying: Boolean,
    val currentPlayingMusicItem: MusicItem,
    val itemQueue: List<MusicItem>
)
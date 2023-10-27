package io.github.takusan23.pieremotecommon.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 音楽アプリのキュー
 *
 * @param queueId ID
 * @param title タイトル
 * @param artist アーティスト
 */
@Serializable
data class MusicItem(
    val queueId: Long,
    val title: String,
    val artist: String
) {

    companion object {

        suspend fun toJsonList(list: List<MusicItem>): String = withContext(Dispatchers.Default) {
            Json.encodeToString(list)
        }

        suspend fun toJson(item: MusicItem): String = withContext(Dispatchers.Default) {
            Json.encodeToString(item)
        }

        suspend fun toItem(json: String): MusicItem = withContext(Dispatchers.Default) {
            Json.decodeFromString<MusicItem>(json)
        }

        suspend fun toItemList(json: String): List<MusicItem> = withContext(Dispatchers.Default) {
            Json.decodeFromString<List<MusicItem>>(json)
        }

    }
}
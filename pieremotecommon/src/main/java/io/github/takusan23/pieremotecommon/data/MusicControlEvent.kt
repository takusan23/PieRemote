package io.github.takusan23.pieremotecommon.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class MusicControlEvent {

    @Serializable
    class Play : MusicControlEvent()

    @Serializable
    class Pause : MusicControlEvent()

    @Serializable
    class Next : MusicControlEvent()

    @Serializable
    class Prev : MusicControlEvent()

    @Serializable
    class MoveIndex(val mediaId: Long) : MusicControlEvent()

    companion object {

        fun toJson(event: MusicControlEvent): String = Json.encodeToString(event)

        fun toData(json: String): MusicControlEvent = Json.decodeFromString(json)

    }
}
package io.github.takusan23.pieremote.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import io.github.takusan23.pieremotecommon.data.MusicItem

@Composable
fun CurrentPlayingComponent(
    modifier: Modifier = Modifier,
    playingItem: MusicItem
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = playingItem.title,
            fontSize = 18.sp,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = playingItem.artist,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}
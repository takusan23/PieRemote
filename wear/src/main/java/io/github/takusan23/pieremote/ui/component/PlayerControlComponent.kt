package io.github.takusan23.pieremote.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import io.github.takusan23.pieremote.R

@Composable
fun PlayerControlComponent(
    isPlaying: Boolean,
    onPrev: () -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Button(onClick = onPrev) {
            Icon(painter = painterResource(id = R.drawable.outline_skip_previous_24), contentDescription = null)
        }
        Button(onClick = if (isPlaying) onPause else onPlay) {
            Icon(painter = painterResource(id = if (isPlaying) R.drawable.outline_pause_24 else R.drawable.outline_play_arrow_24), contentDescription = null)
        }
        Button(onClick = onNext) {
            Icon(painter = painterResource(id = R.drawable.outline_skip_next_24), contentDescription = null)
        }
    }
}
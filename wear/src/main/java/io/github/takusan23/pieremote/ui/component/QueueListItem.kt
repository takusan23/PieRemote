package io.github.takusan23.pieremote.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import io.github.takusan23.pieremotecommon.data.MusicItem

@Composable
fun QueueListItem(
    modifier: Modifier = Modifier,
    musicItem: MusicItem,
    onClick: (MusicItem) -> Unit
) {
    Chip(
        modifier = modifier.fillMaxWidth(),
        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface),
        label = { Text(text = musicItem.title, maxLines = 1) },
        secondaryLabel = { Text(text = musicItem.artist, maxLines = 1) },
        onClick = { onClick(musicItem) }
    )
}
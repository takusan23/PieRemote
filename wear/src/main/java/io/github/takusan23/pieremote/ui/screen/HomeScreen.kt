package io.github.takusan23.pieremote.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import io.github.takusan23.pieremote.ui.component.CurrentPlayingComponent
import io.github.takusan23.pieremote.ui.component.PlayerControlComponent
import io.github.takusan23.pieremotecommon.DataSyncTool
import io.github.takusan23.pieremotecommon.data.MusicControlEvent
import io.github.takusan23.pieremotecommon.WearMessageTool
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onClickQueue: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    val pieRemoteData = DataSyncTool.receivePieRemoteData(context).collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            state = listState,
            autoCentering = AutoCenteringParams(itemIndex = 0),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {

            if (pieRemoteData.value != null) {

                item {
                    CurrentPlayingComponent(playingItem = pieRemoteData.value!!.currentPlayingMusicItem)
                }

                item {
                    PlayerControlComponent(
                        isPlaying = pieRemoteData.value?.isPlaying == true,
                        onPrev = { scope.launch { WearMessageTool.sendMusicControlEvent(context, MusicControlEvent.Prev()) } },
                        onPause = { scope.launch { WearMessageTool.sendMusicControlEvent(context, MusicControlEvent.Pause()) } },
                        onPlay = { scope.launch { WearMessageTool.sendMusicControlEvent(context, MusicControlEvent.Play()) } },
                        onNext = { scope.launch { WearMessageTool.sendMusicControlEvent(context, MusicControlEvent.Next()) } }
                    )
                }

                item {
                    Chip(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface),
                        label = { Text(text = "再生キュー") },
                        onClick = onClickQueue
                    )
                }

            } else {

                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

            }

        }
    }
}
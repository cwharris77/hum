package com.hum.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hum.app.SongState
import com.hum.app.ui.theme.HumColors
import kotlinx.coroutines.delay

@Composable
fun CircleScreen(
    songState: SongState,
    modifier: Modifier = Modifier,
) {
    var recordingSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(songState.isRecording) {
        recordingSeconds = 0
        while (songState.isRecording) {
            delay(1000)
            recordingSeconds++
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HumColors.BgPrimary),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Untitled",
                color = HumColors.TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 100.dp),
            ) {
                itemsIndexed(songState.rows) { index, row ->
                    StemRowView(
                        name = row.name,
                        color = row.color,
                        circles = row.circles,
                        isSelected = index == songState.selectedRowIndex,
                        playingCircleId = songState.playingCircleId,
                        onRowTap = { songState.selectRow(index) },
                        onCircleTap = { songState.toggleCircle(it) },
                    )
                }

                item {
                    TextButton(
                        onClick = { songState.addRow() },
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        Text(
                            text = "+ New stem",
                            color = HumColors.TextSecondary,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }

        RecordButton(
            isRecording = songState.isRecording,
            elapsedSeconds = recordingSeconds,
            onClick = { songState.toggleRecording() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )
    }
}

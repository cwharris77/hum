package com.hum.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hum.app.model.CircleState

@Composable
fun CircleView(
    index: Int,
    state: CircleState,
    color: Color,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val size = if (state == CircleState.Muted) 48.dp else 56.dp
    val alpha = if (state == CircleState.Muted) 0.4f else 1f

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
            .clickable(onClick = onClick)
            .then(
                if (isPlaying) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = color,
                            style = Stroke(width = 3.dp.toPx()),
                            radius = this.size.minDimension / 2,
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${index + 1}",
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}

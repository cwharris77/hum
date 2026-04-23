package com.hum.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hum.app.model.Circle
import com.hum.app.ui.theme.HumColors

@Composable
fun StemRowView(
    name: String,
    color: Color,
    circles: List<Circle>,
    isSelected: Boolean,
    playingCircleId: String?,
    onRowTap: () -> Unit,
    onCircleTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            color = if (isSelected) color else HumColors.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier
                .width(64.dp)
                .clickable(onClick = onRowTap),
        )

        if (circles.isEmpty()) {
            Text(
                text = "Tap record to add a take",
                color = HumColors.TextSecondary.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                itemsIndexed(circles) { index, circle ->
                    CircleView(
                        index = index,
                        state = circle.state,
                        color = color,
                        isPlaying = circle.id == playingCircleId,
                        onClick = { onCircleTap(circle.id) },
                    )
                }
            }
        }
    }
}

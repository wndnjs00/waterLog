package com.app.presentation.ui.Screen.Main.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.presentation.ui.Screen.Main.ChartType

@Composable
fun ChartSegmentControl(
    selectedChart: ChartType,
    onSelectedChange: (ChartType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Gray.copy(alpha = 0.2f))
            .padding(4.dp)
    ){
        SegmentItem(
            text = "주간 수분 섭취량",
            selected = selectedChart == ChartType.WEEKLY,
            modifier = Modifier.weight(1f),
        ) {
            onSelectedChange(ChartType.WEEKLY)
        }

        SegmentItem(
            text = "월간 평균 섭취량",
            selected = selectedChart == ChartType.MONTHLY,
            modifier = Modifier.weight(1f)
        ) {
            onSelectedChange(ChartType.MONTHLY)
        }
    }
}
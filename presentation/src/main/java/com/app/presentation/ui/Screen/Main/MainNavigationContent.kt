package com.app.presentation.ui.Screen.Main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.domain.model.WaterLog
import com.app.presentation.ui.Screen.Main.chart.ChartSegmentControl
import com.app.presentation.ui.Screen.Main.chart.MonthlyChartScreen
import com.app.presentation.ui.Screen.Main.chart.WeeklyChartScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigationContent (
    log: WaterLog,
    streakDays: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChart by remember { mutableStateOf(ChartType.WEEKLY) }

    Column (
        modifier = modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        CircularWaterProgress(
            cups = log.cups,
            target = log.targetCups,
            streak = streakDays,
        )

        Spacer(modifier = Modifier.height(24.dp))

        WaterControlSection(
            cups = log.cups,
            onAdd = onAdd,
            onRemove = onRemove,
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChartSegmentControl(
                selectedChart = selectedChart,
                onSelectedChange = { selectedChart = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedChart) {
                ChartType.WEEKLY -> WeeklyChartScreen()
                ChartType.MONTHLY -> MonthlyChartScreen()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

enum class ChartType {
    WEEKLY,
    MONTHLY,
}
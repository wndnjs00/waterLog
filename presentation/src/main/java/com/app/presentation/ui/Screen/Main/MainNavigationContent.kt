package com.app.presentation.ui.Screen.Main

import MonthlyChartScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.domain.model.WaterLog

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigationContent (
    log: WaterLog,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        CircularWaterProgress(
            cups = log.cups,
            target = log.targetCups,
        )

        Spacer(modifier = Modifier.height(24.dp))

        WaterControlSection(
            cups = log.cups,
            onAdd = onAdd,
            onRemove = onRemove,
        )

        Spacer(modifier = Modifier.height(24.dp))

        WeeklyChartScreen()
        MonthlyChartScreen()
    }
}
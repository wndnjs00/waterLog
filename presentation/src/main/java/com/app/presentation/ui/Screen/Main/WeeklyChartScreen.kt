package com.app.presentation.ui.Screen.Main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.viewModel.MainViewModel
import com.app.presentation.viewModel.WaterViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyChartScreen(
    waterViewModel: WaterViewModel = hiltViewModel(),
    accountViewModel: MainViewModel = hiltViewModel(),
) {
    val logs by waterViewModel.weeklyLogs.collectAsState()
    val userInfo by accountViewModel.userInfo.collectAsState()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),

        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setFitBars(true)

                axisRight.isEnabled = false

                axisLeft.apply {
                    axisMinimum = 0f
                    granularity = 1f
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelCount = 7
                }

                legend.isEnabled = false
            }
        },
        update = { chart ->
            val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")

            val logMap = logs.associateBy {
                LocalDate.parse(it.date).dayOfWeek.value - 1
            }

            val entries = (0..6).map { index ->
                BarEntry(index.toFloat(), logMap[index]?.cups?.toFloat() ?: 0f)
            }

            val dataSet = BarDataSet(entries, "")
            dataSet.color = MainBlue.toArgb()

            val data = BarData(dataSet)
            data.barWidth = 0.6f

            chart.data = data

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(weekDays)
            chart.xAxis.axisMinimum = -0.5f
            chart.xAxis.axisMaximum = 6.5f
            chart.setFitBars(true)
            chart.setDrawGridBackground(false)
            chart.setExtraOffsets(8f, 8f, 8f, 8f)

            // 목표선 초기화 후 추가
            chart.axisLeft.removeAllLimitLines()

            userInfo?.dailyGoal?.let { goal ->
                val limitLine = LimitLine(goal.toFloat(), "목표 $goal 잔")
                limitLine.lineWidth = 2f
                chart.axisLeft.addLimitLine(limitLine)
            }

            chart.invalidate()
        })
}

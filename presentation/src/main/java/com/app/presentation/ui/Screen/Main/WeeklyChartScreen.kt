package com.app.presentation.ui.Screen.Main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.viewModel.MainViewModel
import com.app.presentation.viewModel.WaterViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

private val weekDaysKey = ExtraStore.Key<List<String>>()
private val goalKey = ExtraStore.Key<Double>()

@Composable
private fun WeeklyChartContent(
    modelProducer: CartesianChartModelProducer,
    goal: Int?,
    modifier: Modifier = Modifier,
) {
    val goalLabel = goal?.let { "목표 $it 잔" } ?: ""
    val goalLineComponent = rememberLineComponent(Fill(MainBlue.toArgb()), 2.dp)
    val goalLine = if (goal != null) {
        HorizontalLine(
            y = { it[goalKey] },
            line = goalLineComponent,
            label = { _ -> goalLabel },
        )
    } else null

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    listOf(rememberLineComponent(Fill(MainBlue.toArgb()), 12.dp))
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { context, x, _ ->
                    (context.model.extraStore[weekDaysKey] ?: emptyList()).getOrNull(x.toInt()) ?: ""
                }
            ),
            decorations = listOfNotNull(goalLine),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyChartScreen(
    waterViewModel: WaterViewModel = hiltViewModel(),
    accountViewModel: MainViewModel = hiltViewModel(),
) {
    val logs by waterViewModel.weeklyLogs.collectAsState()
    val userInfo by accountViewModel.userInfo.collectAsState()

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(logs, userInfo) {
        val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")
        val logMap = logs.associateBy {
            LocalDate.parse(it.date).dayOfWeek.value - 1
        }
        val yValues = (0..6).map { index ->
            (logMap[index]?.cups?.toFloat() ?: 0f).toDouble()
        }

        modelProducer.runTransaction {
            columnSeries { series(yValues) }
            extras { extras ->
                extras[weekDaysKey] = weekDays
                userInfo?.dailyGoal?.let { goal ->
                    extras[goalKey] = goal.toDouble()
                } ?: run {
                    extras.remove(goalKey)
                }
            }
        }
    }

    WeeklyChartContent(
        modelProducer = modelProducer,
        goal = userInfo?.dailyGoal,
    )
}

@Preview(showBackground = true)
@Composable
private fun WeeklyChartScreenPreview() {
    val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")
    val sampleYValues = listOf(3.0, 5.0, 2.0, 8.0, 4.0, 6.0, 5.0)
    val previewGoal = 8

    val modelProducer = remember {
        CartesianChartModelProducer().apply {
            runBlocking {
                runTransaction {
                    columnSeries { series(sampleYValues) }
                    extras { extras ->
                        extras[weekDaysKey] = weekDays
                        extras[goalKey] = previewGoal.toDouble()
                    }
                }
            }
        }
    }

    WeeklyChartContent(
        modelProducer = modelProducer,
        goal = previewGoal,
    )
}

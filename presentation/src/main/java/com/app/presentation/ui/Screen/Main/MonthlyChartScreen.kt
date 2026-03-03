package com.app.presentation.ui.Screen.Main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import kotlinx.coroutines.runBlocking
import com.app.domain.model.WaterLog
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.viewModel.WaterViewModel
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.time.LocalDate

private val weekLabelsKey = ExtraStore.Key<List<String>>()

/** Y축 0~10 고정 (WeeklyChartContent와 동일). Composable 밖에 두어 draw 단계에서 Snapshot 읽기 방지. */
private val fixedYRangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = 0.0
    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = 10.0
}

@Composable
private fun MonthlyChartContent(
    modelProducer: CartesianChartModelProducer,
    monthAvg: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        listOf(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(MainBlue.toArgb())),
                                pointProvider = LineCartesianLayer.PointProvider.single(
                                    LineCartesianLayer.Point(
                                        component = rememberShapeComponent(
                                            fill = Fill(MainBlue.toArgb()),
                                            shape = CorneredShape(
                                                topLeft = CorneredShape.Corner.Rounded,
                                                topRight = CorneredShape.Corner.Rounded,
                                                bottomRight = CorneredShape.Corner.Rounded,
                                                bottomLeft = CorneredShape.Corner.Rounded,
                                            ),
                                        ),
                                    )
                                ),
                            )
                        )
                    ),
                    rangeProvider = fixedYRangeProvider,
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { context, x, _ ->
                        (context.model.extraStore[weekLabelsKey] ?: emptyList())
                            .getOrNull(x.toInt()) ?: ""
                    }
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )

        Text(
            text = "이번 달 평균: %.1f잔/일".format(monthAvg),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyChartScreen(
    viewModel: WaterViewModel = hiltViewModel()
) {
    val logs by viewModel.monthlyLogs.collectAsState()

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(logs) {
        val weeklyAvg = calculateMonthlyWeeklyAverage(logs)
        val weekLabels = listOf("1주차", "2주차", "3주차", "4주차")

        modelProducer.runTransaction {
            lineSeries {
                series(weeklyAvg.map { it.toDouble() })
            }
            extras { extras ->
                extras[weekLabelsKey] = weekLabels
            }
        }
    }

    val monthAvg =
        if (logs.isNotEmpty())
            logs.sumOf { it.cups } / logs.size.toFloat()
        else 0f

    MonthlyChartContent(
        modelProducer = modelProducer,
        monthAvg = monthAvg,
    )
}

@Preview(showBackground = true)
@Composable
private fun MonthlyChartScreenPreview() {
    // 실제 데이터가 들어갔을 때와 동일한 형태의 샘플 데이터 (주차별 평균 잔/일)
    val sampleWeeklyAvg = listOf(4.5, 6.0, 5.0, 7.2)
    val weekLabels = listOf("1주차", "2주차", "3주차", "4주차")
    val monthAvg = sampleWeeklyAvg.average().toFloat()

    val modelProducer = remember {
        CartesianChartModelProducer().apply {
            runBlocking {
                runTransaction {
                    lineSeries {
                        series(sampleWeeklyAvg.map { it.toDouble() })
                    }
                    extras { extras ->
                        extras[weekLabelsKey] = weekLabels
                    }
                }
            }
        }
    }

    MonthlyChartContent(
        modelProducer = modelProducer,
        monthAvg = monthAvg,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateMonthlyWeeklyAverage(logs: List<WaterLog>): List<Float> {
    val grouped = logs.groupBy {
        val day = LocalDate.parse(it.date).dayOfMonth
        when (day) {
            in 1..7 -> 1
            in 8..14 -> 2
            in 15..21 -> 3
            else -> 4
        }
    }

    return (1..4).map { week ->
        val weekLogs = grouped[week] ?: emptyList()
        if (weekLogs.isNotEmpty())
            weekLogs.sumOf { it.cups } / weekLogs.size.toFloat()
        else 0f
    }
}

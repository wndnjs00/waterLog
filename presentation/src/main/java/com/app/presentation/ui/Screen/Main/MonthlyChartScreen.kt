import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.domain.model.WaterLog
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.viewModel.WaterViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyChartScreen(
    viewModel: WaterViewModel = hiltViewModel()
) {
    val logs by viewModel.monthlyLogs.collectAsState()

    Column {

        AndroidView(
            modifier = Modifier.fillMaxWidth().height(260.dp),

            factory = { context ->
                LineChart(context).apply {

                    description.isEnabled = false
                    setDrawGridBackground(false)

                    axisRight.isEnabled = false

                    axisLeft.apply {
                        axisMinimum = 0f
                        granularity = 1f
                    }

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        granularity = 1f
                        labelCount = 4
                    }

                    legend.isEnabled = false
                }
            },
            update = { chart ->

                val weeklyAvg = calculateMonthlyWeeklyAverage(logs)

                val entries = weeklyAvg.mapIndexed { index, avg ->
                    Entry(index.toFloat(), avg)
                }

                val dataSet = LineDataSet(entries, "")
                dataSet.color = MainBlue.toArgb()
                dataSet.setCircleColor(MainBlue.toArgb())
                dataSet.lineWidth = 3f
                dataSet.circleRadius = 5f
                dataSet.setDrawValues(false)
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                dataSet.setDrawFilled(true)
                dataSet.fillAlpha = 80

                chart.data = LineData(dataSet)

                chart.xAxis.valueFormatter =
                    IndexAxisValueFormatter(listOf("1주차", "2주차", "3주차", "4주차"))

                chart.xAxis.axisMinimum = -0.5f
                chart.xAxis.axisMaximum = 3.5f

                chart.invalidate()
            }
        )

        val monthAvg =
            if (logs.isNotEmpty())
                logs.sumOf { it.cups } / logs.size.toFloat()
            else 0f

        Text(
            text = "이번 달 평균: %.1f잔/일".format(monthAvg),
            modifier = Modifier.padding(16.dp)
        )
    }
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


package com.unchil.oceanwaterinfo.chart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.unchil.oceanwaterinfo.ChartData
import com.unchil.oceanwaterinfo.getColors
import io.github.koalaplot.core.bar.DefaultBar
import io.github.koalaplot.core.bar.StackedVerticalBarPlot
import io.github.koalaplot.core.bar.VerticalBarPlotStackedPointEntry
import io.github.koalaplot.core.xygraph.XYGraphScope
import kotlin.String
import kotlin.collections.List

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XYGraphScope<Int, Long>.StackedVerticalBarChart(
    chartData: ChartData,
    usableTooltips: Boolean,
    barWidth: Float = 0.9f
){


    when(chartData){
        is ChartData.XYPlotIntLong -> {

            val entries = chartData.data.first
            val data = chartData.data.second

            val desc = chartData.data.third["info"] as List<List<Map<String, Any>>>
            val months = chartData.data.third["yAxisEntries"] as List<String>
            val colors = getColors(months)

            fun barChartEntries(): List<VerticalBarPlotStackedPointEntry<Int, Long>> =
                entries.mapIndexed { entriesIndex, item ->
                    object : VerticalBarPlotStackedPointEntry<Int, Long> {
                        override val x: Int = item
                        override val yOrigin: Long = 0L
                        override val y: List<Long> = object : AbstractList<Long>() {
                            override val size: Int
                                get() = data.size

                            override fun get(index: Int): Long = data.subList(0, index + 1).fold(0L) { accumulator, element ->
                                    accumulator + element[entriesIndex]
                                }
                        }
                    }
                }

            val barChartEntries = barChartEntries()

            StackedVerticalBarPlot(
                barChartEntries,
                barWidth = barWidth,
                bar = { _, barIndex, pointValue ->

                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                        tooltip = {

                            if (usableTooltips){

                                PlainTooltip {
                                    Column{
                                        Text("${ pointValue.x}")
                                        if( desc[barIndex][entries.indexOf(pointValue.x)]["spmon"] != null){
                                            Text("month:${ desc[barIndex][entries.indexOf(pointValue.x)]["spmon"]}")
                                        }
                                        if( desc[barIndex][entries.indexOf(pointValue.x)]["year"] != null){
                                            Text("year:${ desc[barIndex][entries.indexOf(pointValue.x)]["year"]}")
                                        }
                                        Text("value:${ pointValue.y.end - pointValue.y.start}")
                                        Text("accumulated:${ pointValue.y.end}")

                                    }
                                }
                            }
                        }                    ,
                        state = rememberTooltipState(),
                    ) {
                        DefaultBar(
                            brush = SolidColor((colors[ months[barIndex] ] ?: Color.Black).copy(alpha = 0.7f) ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }



                },
            )

        }
        else -> {}
    }


}
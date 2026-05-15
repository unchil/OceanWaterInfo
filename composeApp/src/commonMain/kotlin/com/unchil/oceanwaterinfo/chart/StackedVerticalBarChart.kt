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
fun XYGraphScope<String, Int>.StackedVerticalBarChart(
    chartData: ChartData,
    usableTooltips: Boolean,
    barWidth: Float = 0.9f
){


    when(chartData){
        is ChartData.XYPlotStringInt -> {

            val entries = chartData.data.first
            val data = chartData.data.second

            val desc = chartData.data.third["info"] as List<List<Map<String, Any>>>
            val months = chartData.data.third["yAxisEntries"] as List<String>
            val colors = getColors(months)

            fun barChartEntries(): List<VerticalBarPlotStackedPointEntry<String, Int>> =
                entries.mapIndexed { entriesIndex, item ->
                    object : VerticalBarPlotStackedPointEntry<String, Int> {
                        override val x: String = item
                        override val yOrigin: Int = 0
                        override val y: List<Int> = object : AbstractList<Int>() {
                            override val size: Int
                                get() = data.size

                            override fun get(index: Int): Int = data.subList(0, index + 1).fold(0) { accumulator, element ->
                                (accumulator + element[entriesIndex])
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
                                        if( desc[barIndex][entries.indexOf(pointValue.x)]["plant"] != null){
                                            Text("plant:${ desc[barIndex][entries.indexOf(pointValue.x)]["plant"]}")
                                        }
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
                            brush = SolidColor((colors[ months[barIndex] ] ?: Color.Black) ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }



                },
            )

        }
        else -> {}
    }


}

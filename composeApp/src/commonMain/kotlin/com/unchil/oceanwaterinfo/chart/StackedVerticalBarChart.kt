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
import com.unchil.oceanwaterinfo.ColorPaletteType
import com.unchil.oceanwaterinfo.LegendColor
import com.unchil.oceanwaterinfo.getColors
import com.unchil.oceanwaterinfo.getIntensityColor
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
    barWidth: Float = 0.9f,
    legendColor: LegendColor =LegendColor()

){


    when(chartData){
        is ChartData.XYPlotStringInt -> {

            val entries = chartData.data.first
            val data = chartData.data.second

            // data: List<List<Int>> (첫 번째 리스트는 연도/시리즈, 두 번째 리스트는 발전소별 값)
            /*
            val maxValue = if (data.isNotEmpty() && data.first().isNotEmpty()) {
                // 1. 발전소 개수만큼 인덱스 범위를 생성 (0..N)
                data.first().indices.maxOf { categoryIndex ->
                    // 2. 모든 연도(series)를 돌며 해당 발전소의 값을 합산
                    data.sumOf { series ->
                        series[categoryIndex].toLong()
                    }
                }
            } else {
                1L // 데이터가 없을 경우 기본값
            }

             */


            val desc = chartData.data.third["info"] as List<List<Map<String, Any>>>
            val colorEntries = chartData.data.third["yAxisEntries"] as List<String>

            val colors = getColors(colorEntries, ColorPaletteType.Sequential, legendColor)
         //   val colors = getColors(colorEntries, ColorPaletteType.Pastel)



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
                bar = { categoryIndex, barIndex, pointValue ->

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
                        /*
                        val baseColor = colors[colorEntries[barIndex]] ?: Color.Black
                        val currentValue = data[barIndex][categoryIndex]
                        val finalColor = getIntensityColor(legendColor, pointValue.y.end.toLong(), maxValue)

                         */


                        DefaultBar(
                         //   brush = SolidColor(finalColor ),
                            brush = SolidColor(colors[ colorEntries[barIndex] ] ?: Color.Black ) ,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }



                },
            )

        }
        else -> {}
    }


}

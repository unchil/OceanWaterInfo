package com.unchil.oceanwaterinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.bar.DefaultBar
import io.github.koalaplot.core.bar.DefaultBarPosition
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotEntry
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.bar.VerticalBarPlotEntry
import io.github.koalaplot.core.line.LinePlot2
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.DefaultPoint
import io.github.koalaplot.core.xygraph.XYGraphScope


@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<String, Float>.BoxPlot(
    data:Any,
    usableTooltips: Boolean,
) {

    BoxPlotChart(data, usableTooltips , BoxPlotRange.Q1_Q3)
    BoxPlotChart(data, usableTooltips, BoxPlotRange.MIN_MAX )
    BoxPlotChart(data, usableTooltips, BoxPlotRange.MIN )
    BoxPlotChart(data, usableTooltips, BoxPlotRange.MAX)
    BoxPlotChart(data, usableTooltips, BoxPlotRange.Q2)
    BoxPlotOutliers(data, usableTooltips)
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<String, Float>.BoxPlotChart(
    data:Any,
    usableTooltips: Boolean,
    range:BoxPlotRange
) {
    val data = (data as Map<String, SeaWaterBoxPlotStat>)

    val colors = getColors(data.keys.toList())

    val dataValues = data.values.toList()

    val barInfo = when(range){
        BoxPlotRange.Q1_Q3 -> {
            Pair(0.5f, BorderStroke(1.dp, Color.Gray))
        }
        BoxPlotRange.MIN_MAX  -> {
            Pair(0.01f, null)
        }
        BoxPlotRange.MIN -> {
            Pair(0.15f, null)
        }
        BoxPlotRange.MAX ->{
            Pair(0.15f, null)
        }
        BoxPlotRange.Q2 -> {
            Pair( 0.4f, null)
        }

    }

    val values : List<VerticalBarPlotEntry<String, Float>> = when(range){
        BoxPlotRange.Q1_Q3 -> {
            data.map { entry ->
                DefaultVerticalBarPlotEntry(
                    entry.key,
                    DefaultBarPosition(entry.value.q1, entry.value.q3)
                )
            }
        }
        BoxPlotRange.MIN_MAX -> {
            data.map { entry ->
                DefaultVerticalBarPlotEntry(
                    entry.key,
                    DefaultBarPosition(entry.value.min, entry.value.max)
                )
            }
        }
        BoxPlotRange.MIN -> {
            data.map { entry ->
                DefaultVerticalBarPlotEntry(
                    entry.key,
                    DefaultBarPosition(entry.value.min, entry.value.min + 0.05f)
                )
            }
        }
        BoxPlotRange.MAX -> {
            data.map { entry ->
                DefaultVerticalBarPlotEntry(
                    entry.key,
                    DefaultBarPosition(entry.value.max - 0.05f, entry.value.max )
                )
            }
        }
        BoxPlotRange.Q2 -> {

            data.map { entry ->
                DefaultVerticalBarPlotEntry(
                    entry.key,
                    DefaultBarPosition(entry.value.median - 0.025f, entry.value.median + 0.025f)
                )
            }
        }
    }

    VerticalBarPlot(
        values,
        bar = { i, _, point ->

            val color = when(range){
                BoxPlotRange.Q1_Q3 -> colors[point.x] ?: Color.Black
                BoxPlotRange.MIN_MAX -> colors[point.x] ?: Color.Black
                BoxPlotRange.MIN, BoxPlotRange.MAX -> Color.Gray
                BoxPlotRange.Q2 -> Color.White
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.End
                ),
                tooltip = {
                    if (usableTooltips && range == BoxPlotRange.Q1_Q3){
                        PlainTooltip (containerColor = DarkGray){

                            Column{

                                val textStyleTitle = TextStyle(
                                    color = Color.White,
                                    fontSize =  12.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                val textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize =  12.sp,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center
                                )


                                BoxPlotTooltips(values[i].x, textStyleTitle)
                                BoxPlotTooltips("max : ${dataValues[i].max}", textStyle)
                                BoxPlotTooltips("75% : ${dataValues[i].q3}", textStyle)
                                BoxPlotTooltips("50% : ${dataValues[i].median}",  textStyle)
                                BoxPlotTooltips("25% : ${dataValues[i].q1}",  textStyle)
                                BoxPlotTooltips("min : ${dataValues[i].min}",  textStyle)



                            }


                        }
                    }
                }                    ,
                state = rememberTooltipState(),
            ) {
                DefaultBar(
                    brush = SolidColor(color ),
                    modifier = Modifier.fillMaxWidth(),
                    border = barInfo.second
                )
            }



        },
        barWidth = barInfo.first
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<String, Float>.BoxPlotOutliers(
    data:Any,
    usableTooltips: Boolean,
){

    val rawData = (data as Map<String, SeaWaterBoxPlotStat>)
    val colors = getColors(rawData.keys.toList())
    val data = rawData.values.map { entry -> entry.outliers }
    val xValues = rawData.keys.toList()


    data.forEachIndexed { index, floats ->

        LinePlot2(
            data =  floats.map {
                DefaultPoint(
                    xValues[index],
                    it
                )
            },
            lineStyle = LineStyle(
                brush = SolidColor(colors[xValues[index]] ?: Color.Black),
                strokeWidth = 0.dp),
            symbol = { point ->
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above),
                    tooltip = {
                        if (usableTooltips) {
                            PlainTooltip { Text("${xValues[index]}\n${point.y}") }
                        }
                    },
                    state = rememberTooltipState(),
                ) {
                    Symbol(
                        shape = ShapeDefaults.ExtraSmall,
                        fillBrush = SolidColor(Color.Gray.copy(alpha = 0.5f)),
                        size = 6.dp,
                    )
                }
            },
        )

    }

}


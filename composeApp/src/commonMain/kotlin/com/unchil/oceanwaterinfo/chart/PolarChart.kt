package com.unchil.oceanwaterinfo.chart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.unchil.oceanwaterinfo.ChartData
import com.unchil.oceanwaterinfo.getColors
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.polar.PolarGraphScope
import io.github.koalaplot.core.polar.PolarPlotSeries2
import io.github.koalaplot.core.util.AngularValue
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.toDegrees

@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PolarGraphScope<AngularValue>.PolarChart(
    chartData: ChartData,
    usableTooltips: Boolean = false,
    usableSymbol: Boolean = true,
){

    when(chartData){
        is ChartData.PolarGraphPlot -> {

            val entries = chartData.data.map { triple -> triple.first }
            val colors = getColors(entries)
            val polarPointList = chartData.data.flatMap {
                listOf(it.third)
            }

            polarPointList.forEachIndexed { index, seriesData ->

                    PolarPlotSeries2(
                        seriesData,
                        symbols = {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                                tooltip = {

                                    PlainTooltip {
                                        Column() {
                                            Text(text = chartData.data[index].first)
                                            Text(text = "latitude:${chartData.data[index].second.y}, longitude:${chartData.data[index].second.x}")
                                            Text(text = "${chartData.data[index].third[0].r} 유속(cm/s)")
                                            Text(text = "${chartData.data[index].third[0].theta.toDegrees()} deg")
                                        }

                                    }

                                },
                                state = rememberTooltipState(),
                            ) {

                                Symbol(
                                    shape = CircleShape,
                                    fillBrush = SolidColor(
                                        colors[entries[index]] ?: Color.LightGray
                                    )
                                )

                            }
                        },
                    )

            }
        }
        else -> {}
    }

}
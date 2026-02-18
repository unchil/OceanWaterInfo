package com.unchil.oceanwaterinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.line.LinePlot2
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.AxisContent
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberDoubleLinearAxisModel


@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun GeoEmptyChart(layout: LayoutData, data: Pair<List<Point<Double, Double>>, Any>){


    Box(
        modifier = Modifier
            .width(layout.size.height*1.2f)
            .height(layout.size.height)
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        ChartLayout(
            modifier = paddingMod
                .background(color = MaterialTheme.colorScheme.background),
            title = { ChartTitle(layout.layout.title, modifier = paddingMod) },
        ) {

            XYGraph(
                rememberDoubleLinearAxisModel(data.first.getRange().first),
                rememberDoubleLinearAxisModel(data.first.getRange().second),
                xAxisContent = AxisContent(
                    labels = { AxisLabel(it.toString(), Modifier.padding(top = 2.dp)) },
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            AxisTitle("Longitude", paddingMod)
                        }
                    },
                    style = AxisStyle(),
                ),
                yAxisContent = AxisContent(
                    labels = { AxisLabel(it.toString(), Modifier.padding(top = 2.dp)) },
                    title = {
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            AxisTitle(
                                "Latitude",
                                modifier = paddingMod
                                    .rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                            )
                        }
                    },
                    style = AxisStyle()
                )
            ) {

                LinePlot2(
                    data.first,
                    lineStyle = LineStyle(SolidColor(Color.Transparent)),
                    symbol = {
                        Symbol(
                            modifier = Modifier,
                            shape = ShapeDefaults.ExtraSmall,
                            fillBrush = SolidColor( Color.Black),
                            size = 2.dp,
                            alpha = 1f
                        )
                    }
                )


            }
        }

    }

}
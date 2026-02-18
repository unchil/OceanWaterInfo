package com.unchil.oceanwaterinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.line.LinePlot2
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.XYGraphScope
import kotlin.Double

@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<Double, Double>.GeoChart(
    data: Any,
    usableTooltips: Boolean = false
) {

    val data = (data as GeoChartDataType)
    val colors = getColors(data.first)


    LinePlot2(
        data.third.first,
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

    val observationsData = data.second.map { triple -> triple.second }

    LinePlot2(
        observationsData,
        lineStyle = LineStyle(SolidColor(Color.Transparent)),
        symbol = { point ->
            val index = observationsData.indexOf(point)
            if(index != -1){
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = {
                        if (usableTooltips) {
                            PlainTooltip {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,) {
                                    Text(text = data.first[index])
                                    Text(text = "${data.second[index].third.second}°C")
                                }
                            }
                        }
                    },
                    state = rememberTooltipState(),
                ) {

                    Symbol(
                        modifier = Modifier.clickable(){
                            data.third.second(point)
                        },
                        shape = ShapeDefaults.Medium,
                        fillBrush = SolidColor(colors.entries.toList()[index].value ),
                        outlineBrush = SolidColor( Color.Black),
                        size = 12.dp,
                        alpha = 0.7f
                    )
                }
            }
        }
    )


}
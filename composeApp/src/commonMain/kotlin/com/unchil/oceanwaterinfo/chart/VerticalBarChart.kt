package com.unchil.oceanwaterinfo

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
import io.github.koalaplot.core.bar.DefaultBar
import io.github.koalaplot.core.bar.DefaultBarPosition
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotEntry
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraphScope

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<String, Float>.VerticalBarChart(
    data: Any,
    usableTooltips: Boolean,
    barWidth: Float = 0.9f
){
    val data = (data as List<Triple< String, Point<String, Float>, Map<String, Any>>>)

    val colors = getColors(data.map { triple -> triple.first })


    val values = data.map { triple ->
        DefaultVerticalBarPlotEntry(  triple.second.x,
            DefaultBarPosition(0f, triple.second.y)
        )
    }

    VerticalBarPlot(
        values,
        bar = { index, _, _ ->

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = {
                    if (usableTooltips){
                        PlainTooltip {
                            Text("${values[index].x}\n${values[index].y.end }" )
                        }
                    }
                }                    ,
                state = rememberTooltipState(),
            ) {
                DefaultBar(
                    brush = SolidColor(colors[data[index].first] ?: Color.Black),
                    modifier = Modifier.fillMaxWidth(),
                )
            }


        },
        barWidth = barWidth
    )
}





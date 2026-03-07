package com.unchil.oceanwaterinfo.chart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.VerticalBarChart
import com.unchil.oceanwaterinfo.formatLongToDateTime
import com.unchil.oceanwaterinfo.getColors
import io.github.koalaplot.core.line.LinePlot2
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraphScope
import oceanwaterinfo.composeapp.generated.resources.Res
import oceanwaterinfo.composeapp.generated.resources.arrow_downward_alt_24px
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<Double, Float>.DegLineChart(
    data: Any,
    usableTooltips: Boolean = false,
    usableSymbol: Boolean = true,
) {

    val data = (data as List<Triple<String, List<Point<Double, Float>>, Map<String, Any>    >>)

    val colors = getColors(data.map { triple -> triple.first })

    val isVisibleSymbol = remember { mutableStateOf(0) }

    val onHoverEvent = { index: Int ->
        isVisibleSymbol.value = index
    }

    val yMax = data.maxOf { entry -> entry.second.maxOf { point -> point.y } }
    val yMin = data.minOf { entry -> entry.second.minOf { point -> point.y } }
    val range = yMin - 1.0f..yMax + 1.0f


    if (usableTooltips) {
        VerticalBarChart(
            data,
            range,
            onHoverEvent
        )
    }

    data.forEachIndexed { index, triple ->

        val strokeWidth = remember { mutableStateOf(1.dp) }
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isUsableSymbolTooltips by interactionSource.collectIsHoveredAsState()
        strokeWidth.value = if (isPressed) 1.dp else 0.5.dp

        LinePlot2(
            data = triple.second,
            lineStyle = LineStyle(
                brush = SolidColor(colors[triple.first] ?: Color.Black),
                strokeWidth = strokeWidth.value,
                alpha = 0.5f,
            ),
            symbol = { point ->

                // 1. 현재 포인트가 호버 상태인지 미리 판별
                val isHovered = isVisibleSymbol.value == triple.second.indexOf(point)


                // 2. 상태에 따른 크기와 투명도 결정
                val symbolSize = when {
                    isPressed -> 20.dp
                    isHovered -> 16.dp
                    isUsableSymbolTooltips -> 16.dp
                    triple.second.indexOf(point) == 0 -> 16.dp
                    triple.second.indexOf(point) == triple.second.lastIndex ->16.dp
                    usableSymbol -> 12.dp
                    else -> 0.dp
                }

                val symbolAlpha = when {
                    isHovered || usableSymbol || triple.second.indexOf(point) == 0 || triple.second.indexOf(
                        point
                    ) == triple.second.lastIndex -> 1.0f

                    else -> 0f
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = {
                        if (isUsableSymbolTooltips) {
                            PlainTooltip {
                                Column() {
                                    Text(
                                        text = formatLongToDateTime(point.x),
                                        textAlign = TextAlign.Center,
                                        fontStyle = FontStyle.Italic
                                    )
                                    Text(text = "${triple.first} : ${point.y}")
                                    Text(text = "deg : ${(triple.third["crdir"] as List<Point<Double,Float>>).first { pair ->
                                        pair.x == point.x
                                    }.y}")
                                    Text(text = "12-hour min : ${triple.second.minOf { point -> point.y }}")
                                    Text(text = "12-hour max : ${triple.second.maxOf { point -> point.y }}")

                                }

                            }
                        }
                    },
                    state = rememberTooltipState(),
                ) {

                    val deg = (triple.third["crdir"] as List<Point<Double,Float>>).first { pair ->
                        pair.x == point.x
                    }.y

                    Icon(
                        painterResource(Res.drawable.arrow_downward_alt_24px),
                        modifier = Modifier.clickable(
                            interactionSource =interactionSource,
                            indication = null, // 리플 효과
                            onClick = {

                            }
                        ).size(symbolSize).rotate(deg + 180f ).alpha(symbolAlpha),
                        contentDescription = "",
                        tint = colors[triple.first] ?: Color.Black
                    )

                }

            },
        ) //LinePlot2

    }
}



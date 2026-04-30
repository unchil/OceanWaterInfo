package com.unchil.oceanwaterinfo.chart

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.ChartData
import com.unchil.oceanwaterinfo.ChartDataList
import com.unchil.oceanwaterinfo.ChartType
import com.unchil.oceanwaterinfo.formatLongToDateTime
import com.unchil.oceanwaterinfo.getColors
import io.github.koalaplot.core.line.LinePlot2
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraphScope
import oceanwaterinfo.composeapp.generated.resources.Res
import oceanwaterinfo.composeapp.generated.resources.arrow_upward_alt_24px
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Suppress("UNCHECKED_CAST")
@Composable
fun XYGraphScope<Double, Float>.DegLineChart(
    chartData: ChartData,
    usableTooltips: Boolean = false,
    usableSymbol: Boolean = true,
) {

    when(chartData) {
        is ChartData.TimeSeries -> {

            val colors = getColors(chartData.data.map { triple -> triple.first })

            val isVisibleSymbol = remember { mutableStateOf(0) }

            val onHoverEvent = { index: Int ->
                isVisibleSymbol.value = index
            }

            if (usableTooltips) {
                VerticalTooltipBar(chartData.data, ChartType.DegLine, onHoverEvent)
            }

            chartData.data.forEachIndexed { index, triple ->

                val strokeWidth = remember { mutableStateOf(1.dp) }
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val isUsableSymbolTooltips by interactionSource.collectIsHoveredAsState()
                strokeWidth.value = if (isPressed) 2.dp else 0.5.dp

                // Compose 애니메이션 값 생성
                val infiniteTransition = rememberInfiniteTransition()
                val phase by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 20f, // 패턴의 총 합(20+10)만큼 이동하면 부드럽게 반복됨
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                LinePlot2(
                    data = triple.second,
                    lineStyle = LineStyle(
                        brush = SolidColor(colors[triple.first] ?: Color.Black),
                        strokeWidth = strokeWidth.value,
                        // [선 길이, 공백 길이] 순서로 입력 (단위: 픽셀)
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(10f, 10f), // 10px 그리고 10px 띔
                            phase = phase
                        ),
                        alpha = 1.0f,
                    ),
                    symbol = { point ->

                        // 1. 현재 포인트가 호버 상태인지 미리 판별
                        val isHovered = isVisibleSymbol.value == triple.second.indexOf(point)


                        // 2. 상태에 따른 크기와 투명도 결정
                        val symbolSize = when {
                            isPressed -> 24.dp
                            isHovered -> 20.dp
                            isUsableSymbolTooltips -> 20.dp
                            triple.second.indexOf(point) == 0 -> 16.dp
                            triple.second.indexOf(point) == triple.second.lastIndex ->16.dp
                            usableSymbol -> 16.dp
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
                                            Text(text = "6-hour min : ${triple.second.minOf { point -> point.y }}")
                                            Text(text = "6-hour max : ${triple.second.maxOf { point -> point.y }}")

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
                                //     painterResource(Res.drawable.baseline_arrow_circle_up_24),
                                painterResource(Res.drawable.arrow_upward_alt_24px),
                                modifier = Modifier.clickable(
                                    interactionSource =interactionSource,
                                    indication = null, // 리플 효과
                                    onClick = {

                                    }
                                ).size(symbolSize).rotate(deg  ).alpha(symbolAlpha),
                                contentDescription = "",
                                tint = colors[triple.first] ?: Color.Black
                            )

                        }

                    },
                ) //LinePlot2

            }
        }
        else -> {}
    }

}



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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<Double, Float>.PointLineChart(
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


        // 1. 해당 데이터 세트의 유량(tm001) 리스트와 최소/최대값을 미리 계산 (0 제외, 크기 비율 계산용)
        val tm001Points = triple.third["tm001"] as List<Point<Double, Float>>
        val tm001Values = tm001Points.map { it.y }.filter { it > 0f }
        val tm001Min = if (tm001Values.isNotEmpty()) tm001Values.minOrNull() ?: 1f else 1f
        val tm001Max = if (tm001Values.isNotEmpty()) tm001Values.maxOrNull() ?: 1f else 1f


        val strokeWidth = remember { mutableStateOf(1.dp) }
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isUsableSymbolTooltips by interactionSource.collectIsHoveredAsState()
        strokeWidth.value = if (isPressed) 2.dp else 0.5.dp


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

                // 2. 현재 포인트의 유량(tm001) 값 찾기
                val tm001Value = tm001Points.firstOrNull { it.x == point.x }?.y ?: 0f


                // 2. 24dp ~ 48dp 사이의 동적 크기 계산
                val minSize = 24f
                val maxSize = 48f

                val dynamicSize = if (tm001Value > 0f) {
                    if (tm001Max > tm001Min) {
                        // 비율 계산 (0.0 ~ 1.0)
                        val ratio = (tm001Value - tm001Min) / (tm001Max - tm001Min)
                        // 24dp + (비율 * 24dp) => 24dp ~ 48dp
                        (minSize + (ratio * (maxSize - minSize))).dp
                    } else {
                        36.dp // 값이 하나뿐이거나 모두 같으면 중간값(36dp)
                    }
                } else {
                    4.dp // 유량이 0인 경우 최소한으로 표시
                }

                val symbolSize = when {
                    isPressed -> 6.dp + dynamicSize
                    isHovered || isUsableSymbolTooltips -> 2.dp + dynamicSize
                    else -> dynamicSize
                }

                val symbolAlpha = when {
                    usableSymbol ||
                            triple.second.indexOf(point) == 0 ||
                            triple.second.indexOf(point ) == triple.second.lastIndex -> 0.5f
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
                                    Text(text = "${triple.first} : ${point.y}(Ph)")
                                    Text(text = "유량 : ${(triple.third["tm001"] as List<Point<Double,Float>>).first { pair ->
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

                    Icon(

                        Icons.Default.Circle,
                        modifier = Modifier.clickable(
                            interactionSource =interactionSource,
                            indication = null, // 리플 효과
                            onClick = {

                            }
                        ).size(symbolSize).alpha(symbolAlpha),
                        contentDescription = "",
                        tint = colors[triple.first] ?: Color.Black
                    )

                }

            },
        ) //LinePlot2

    }
}
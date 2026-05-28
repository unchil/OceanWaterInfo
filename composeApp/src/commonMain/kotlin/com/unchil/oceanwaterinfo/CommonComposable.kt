package com.unchil.oceanwaterinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp



@Composable
fun AirQualityStatusBoard(currentLevel: Int, stat:List<Pair<Int,Int>>) {

    val statusList = listOf(
        "안전" to Color(0xFF4CAF50),      // Green
        "주의" to Color(0xFFFFEB3B),      // Yellow
        "매우주의" to Color(0xFFFF9800),   // Orange
        "위험" to Color(0xFFF44336),      // Red
        //"심각" to Color(0xFF9C27B0)       // Purple
        "심각" to Color.Red   // Purple
    )


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
    ) {

        statusList.forEachIndexed { index, (label, color) ->

            val count = stat.firstOrNull{ it.first.equals(index) }?.second ?: 0

            val text = if(count == 0) label else "${label}:${count}"

            val isSelected = index == currentLevel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(if (isSelected) color else color.copy(alpha = 0.15f))
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.outline else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) {
                        if (index == 1) Color.Black else Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChartOptionToggle(
    labelText:String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
){
    ToggleButton(
        checked = isChecked,
        colors = ToggleButtonDefaults.toggleButtonColors(
            checkedContainerColor  = Color.LightGray,
            checkedContentColor  = Color.Black,
        ) ,
        shapes = ToggleButtonShapes(
            shape = ShapeDefaults.ExtraExtraLarge,
            pressedShape = ShapeDefaults.ExtraExtraLarge,
            checkedShape = ShapeDefaults.ExtraExtraLarge,
        ),
        onCheckedChange = onCheckedChange,
    ){
        Text(  text = labelText  )
    }
}


/**
 * 공통으로 사용할 드래그 가능한 구분선 컴포저블
 */
@Composable
fun DraggableVerticalDivider(onDrag: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .width(8.dp) // 드래그 감지 영역
            .fillMaxHeight()
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        VerticalDivider(thickness = 1.dp, color = Color.LightGray)
    }
}

@Composable
fun DraggableHorizontalDivider(onDrag: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .height(8.dp) // 드래그 감지 영역
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
    }
}
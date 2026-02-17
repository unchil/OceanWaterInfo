package com.unchil.oceanwaterinfo

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

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
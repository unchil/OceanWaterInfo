package com.unchil.oceanwaterinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CoastalFloodingMap(
    selectedGradeIndex: Int,
    selectedSidoIndex: Int,
    tabClick:(
        tapType:String,
        gradeOption: CoastalFloodingGrade?,
        sidoOption: SiDo?,
        tabIndex:Int) -> Unit,
    content: @Composable @UiComposable (BoxWithConstraintsScope.() -> Unit)
){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =   Arrangement.Top
    ) {

        Text(
            "Korea Coastal Flooding Prediction Information",
            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )


        SecondaryTabRow(
            selectedTabIndex = selectedGradeIndex,
            containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
            contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
        ) {
            CoastalFloodingGrade.entries.forEachIndexed { index, element ->
                Tab(
                    selected = selectedGradeIndex == index,
                    onClick = {
                        tabClick("first",element, null, index )
                    },
                    text = {
                        Text(
                            text = element.tabTitle(),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }


        SecondaryTabRow(
            selectedTabIndex = selectedSidoIndex,
            containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
            contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
        ) {
            SiDo.entries.forEachIndexed { index, element ->
                Tab(
                    selected = selectedSidoIndex == index,
                    onClick = {
                        tabClick("second",null, element , index)
                    },
                    text = {
                        Text(
                            text = element.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            content()
        }


    }
}



@Composable
fun AlertBoxDataNotFound(
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(6.dp).fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {

        Text(
            text = "선택하신 지역 및 등급에 대한 침수 예상 데이터가 존재하지 않습니다.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        )
    }
}

@Composable
fun AirQualityStatusBoard(
    airQualityStage: AirQualityManager.AirQualityStage,
    stat:List<Pair<AirQualityManager.AirQualityStage,Int>>
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
        ,horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp)
    ) {

        AirQualityManager.AirQualityStage.entries.forEach { airQS ->

            val count = stat.firstOrNull{
                it.first.level == airQS.level
            }?.second ?: 0
            val isSelected = airQS.equals(airQualityStage)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(if (isSelected) airQS.argbColor else airQS.argbColor.copy(alpha = 0.15f))
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.outline else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 4.dp)
                ,contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Text(
                        text = airQS.titleKo,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            if(airQS.level == 6 || airQS.level == 4) Color.White else Color.Black
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )

                    Text(
                        text = "${count}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            if(airQS.level == 6 || airQS.level == 4) Color.White else Color.Black
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )


                }


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
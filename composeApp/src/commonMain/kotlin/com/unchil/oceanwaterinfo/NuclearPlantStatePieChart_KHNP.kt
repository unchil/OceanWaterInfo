package com.unchil.oceanwaterinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhnpPlantStateViewModel
import io.github.koalaplot.core.pie.BezierLabelConnector
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun NuclearPlantStatePieChart_KHNP(){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpPlantStateViewModel = remember { KhnpPlantStateViewModel() }

    val plantstates = viewModel._khnpPlantState.collectAsState()

    val filteredPlantState =  remember {
        mutableStateOf(Triple(emptyList<Float>(), emptyList<Color>(), emptyMap<String,Any>()) )
    }
    var selectedOption by remember { mutableStateOf(POWER_PLANT_AREA.POWER_PLANT.entries[0]) }
    val onSelection: ( POWER_PLANT_AREA.POWER_PLANT ) -> Unit = { entry ->
        selectedOption = entry
    }

    LaunchedEffect(viewModel){
        while(true){
            viewModel.onEvent(KhnpPlantStateViewModel.Event.Refresh)
            delay(24 * 60 * 60 * 1000L)
        }
    }

    LaunchedEffect(key1= plantstates.value,  key2=selectedOption){
        if(plantstates.value.size > 0){
            filteredPlantState.value = plantstates.value
                .filter {  it.genName.equals(selectedOption.name) }
                .sortedBy{ it.unitNm}
                .toKHNPPlantState()
        }
    }


    if(filteredPlantState.value.first.isNotEmpty()){

        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){

            ChartTitle("Nuclear Plant States")

            var selectedTabIndex by remember { mutableIntStateOf(0) }
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
                contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
            ) {
                POWER_PLANT_AREA.POWER_PLANT.entries.forEachIndexed { index, entrie ->

                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            onSelection(entrie)
                        },
                        text = {
                            Text(
                                text = entrie.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.padding(10.dp))


            PieChart(
                    values = filteredPlantState.value.first, // 모든 발전소를 동일한 크기로 표시
                    slice = { index ->
                        DefaultSlice(
                            color = filteredPlantState.value.second[index],
                            border =
                                BorderStroke(
                                    2.dp,
                                    lerp(
                                        filteredPlantState.value.second[index],
                                        Color.LightGray,
                                        0.2f
                                    ),
                                ),
                            hoverExpandFactor = 1.05f,
                            antiAlias = true,
                            gap = 0.1f,
                        )
                    },
                    label = { index ->
                        val info =
                            (filteredPlantState.value.third["info"] as List<KHNPPlantOperationInfo>)[index]
                        Text("${info.unitNm}:${info.unitCd}\n(${info.unitSt})")
                    },
                    labelConnector = { index ->
                        BezierLabelConnector()
                    },
                    holeSize = 0.6f,
                    holeContent = {
                        val data =
                            (filteredPlantState.value.third["info"] as List<KHNPPlantOperationInfo>)
                        val info = data.first()
                        // 1. 상태별 개수 집계 (운전, 정비, 정지 등)
                        val statusGroups = data.groupingBy { it.unitSt }.eachCount()
                        // 2. 표시할 텍스트 생성
                        val statsText = statusGroups.entries.joinToString("\n") { (status, count) ->
                            val percentage = (count.toFloat() / data.size * 100).toInt()
                            "$status: $percentage%"
                        }

                        val text = "${info.genName}\n${info.siteCd}\n${
                            Instant.parse(info.unitDttm).toLocalDateTime(
                                TimeZone.currentSystemDefault()
                            ).date
                        }\n" + statsText

                        Box(
                            modifier = Modifier.fillMaxSize().padding(it),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = text,
                                modifier = Modifier.fillMaxSize(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                    },
                )

            }




    }



}
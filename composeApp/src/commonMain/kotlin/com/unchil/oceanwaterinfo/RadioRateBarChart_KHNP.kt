package com.unchil.oceanwaterinfo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.unchil.oceanwaterinfo.viewmodel.KhnpRadioRateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RadioRateBarChart(){

    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpRadioRateViewModel = remember {
        KhnpRadioRateViewModel(  coroutineScope  )
    }

    val onRefresh:()->Unit = {
        coroutineScope.launch {
            while(true){
                delay(5 * 60 * 1000L).let{
                    viewModel.onEvent(KhnpRadioRateViewModel.Event.Refresh)
                }
            }
        }
    }


    val radioRateInfo = viewModel._khnpRadioRateStateFlow.collectAsState()
    var selectedOption by remember { mutableStateOf(POWER_PLANT_AREA.POWER_PLANT.entries[0]) }
    val onSelection: ( POWER_PLANT_AREA.POWER_PLANT ) -> Unit = { entry ->
        selectedOption = entry
    }

    val chartData: MutableState<  ChartDataListStringFloat> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= radioRateInfo.value,  key2=selectedOption){
        if(radioRateInfo.value.isNotEmpty()){
            chartData.value = radioRateInfo.value.filter {
                it.genName.equals(selectedOption.name)
            }.toBarChartTripleList(
                nameSelector = { it.expl },
                primaryValueSelector = {it.value.trim().toFloatOrNull() ?: 0f},
                secondaryValueSelector = { Triple(it.genName, it.name, it.time) }
            )
        }
    }

    if(chartData.value.isNotEmpty()){
        ChartDataFlow(
            chartData = ChartData.XYPlotStringFloat(chartData.value),
            title = "Power Plant Radio Rate",
            xTitle = "Name",
            yTitle = "Radio Rate",
            caption = "from https://www.data.go.kr/data/15157701/openapi.do",
            chartType = ChartType.VerticalBar,
            yRangePadding = 0.05f,
            legendTitle = "Name",
            onRefresh = onRefresh
        ){

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
                                style = MaterialTheme.typography.titleSmall // 보조 탭에 맞는 스타일
                            )
                        }
                    )
                }
            }

        }
    }

}



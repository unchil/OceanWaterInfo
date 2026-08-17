package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.unchil.oceanwaterinfo.viewmodel.KhnpRadioRateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RadioRateBarChart(){

    val coroutineScope = rememberCoroutineScope()

    val viewModel: KhnpRadioRateViewModel = remember {
        KhnpRadioRateViewModel(  )
    }


    val onReload:()->Unit = {

        coroutineScope.launch {
            viewModel.onEvent(KhnpRadioRateViewModel.Event.Refresh)
        }
    }



    LaunchedEffect(viewModel){
        while(true){

            viewModel.onEvent(KhnpRadioRateViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }


    val radioRateInfo = viewModel._khnpRadioRateStateFlow.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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


    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {



        // [Reload, Tooltips, Symbol, Legend]
        val bottomBarOpt = listOf(true, true, false, true)
        ChartDataFlow(
            chartData = ChartData.XYPlotStringFloat(chartData.value),
            title = "Power Plant Radio Rate",
            xTitle = "Name",
            yTitle = "Radio Rate",
            caption = "from https://www.data.go.kr/data/15157701/openapi.do",
            chartType = ChartType.VerticalBar,
            yRangePadding = 0.05f,
            legendTitle = "Name",
            onReload = onReload,
            bottomBarOpt = bottomBarOpt
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



        AnimatedVisibility(isLoading){
            CircularProgressIndicator(
                color = Color.DarkGray
            )
        }

    } //Box



}



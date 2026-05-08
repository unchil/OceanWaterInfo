package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhnpRadioActiveWasteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RadioActiveWastePlantStatStackedBarChart_KHNP(){

    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpRadioActiveWasteViewModel = remember {
        KhnpRadioActiveWasteViewModel(  coroutineScope  )
    }

    val onRefresh:()->Unit = {
        coroutineScope.launch {
            while(true){
                delay(24 * 60 * 60 * 1000L).let{
                    viewModel.onEvent(KhnpRadioActiveWasteViewModel.Event.Refresh)
                }
            }
        }
    }

    val radioActiveWasteInfo = viewModel._khnpRadioActiveWasteStateFlow.collectAsState()
    val chartData: MutableState< ChartDataIntLong> = remember { mutableStateOf(Triple(emptyList(), emptyList(), emptyMap())) }

    LaunchedEffect(key1= radioActiveWasteInfo.value){
        if(radioActiveWasteInfo.value.isNotEmpty()){
            val transData = radioActiveWasteInfo.value.toKHNPRadioActiveWastePlant().sortedBy{it.year}
            val yAxisEntries = transData.map { it.year }.distinct().sorted()

            chartData.value = transData.toStackedBarChartTripleList(
                entriesSelector = { it.plant },
                groupBySelect = { it.year },
                filterSelect = { it.plant },
                primaryValueSelector = { it.total },
                secondaryValueSelector = { mapOf("plant" to it.plant, "year" to it.year) },
                yAxisEntries = { yAxisEntries }
            )
        }

    }



    ChartDataFlow(
        chartData = ChartData.XYPlotIntLong(chartData.value),
        title = "Power Plant Radio Active Waster (Year)",
        xTitle = "Plant",
        yTitle = "RadioActiveWaster",
        caption = "from https://www.data.go.kr/data/15157707/openapi.do",
        height = 600.dp,
        chartType = ChartType.StackedVerticalBar,
        legendTitle = "Year",
        onRefresh = onRefresh
    )


}
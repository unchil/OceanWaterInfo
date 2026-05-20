package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
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
    val chartData: MutableState< ChartDataStringInt> = remember { mutableStateOf(Triple(emptyList(), emptyList(), emptyMap())) }

    LaunchedEffect(key1= radioActiveWasteInfo.value){
        if(radioActiveWasteInfo.value.isNotEmpty()){
            val transData = radioActiveWasteInfo.value.toKHNPRadioActiveWastePlant().sortedBy{it.year}
            val yAxisEntries = transData.map { it.year }.distinct().sorted()

            chartData.value = transData.toStackedBarChartTripleList(
                entriesSelector = {  it.genName},
                groupBySelect = { it.year },
                filterSelect = { it.genName },
                primaryValueSelector = { it.total.toInt() },
                secondaryValueSelector = { mapOf("plant" to it.plant, "year" to it.year, "genName" to it.genName) },
                yAxisEntries = { yAxisEntries }
            )
        }

    }



    ChartDataFlow(
        chartData = ChartData.XYPlotStringInt(chartData.value),
        title = "Power Plant Radio Active Waste (Plant)",
        xTitle = "Plant",
        yTitle = "RadioActiveWaste",
        caption = "from https://www.data.go.kr/data/15157707/openapi.do",
        chartType = ChartType.StackedVerticalBar,
        legendTitle = "Year",
        legendColor = LegendColor(start=Color.Blue, end=Color.Red),
        onRefresh = onRefresh
    )


}
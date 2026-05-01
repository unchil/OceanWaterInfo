package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WaterTempTimeSeries_KHOA(){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaObservationViewModel = remember {
        KhoaObservationViewModel(  coroutineScope  )
    }

    val onRefresh:()->Unit = {
        coroutineScope.launch {
            while(true){
                delay(1 * 60 * 1000L).let{
                    viewModel.onEvent(KhoaObservationViewModel.Event.Refresh)
                }
            }
        }
    }

    val seaWaterInfo = viewModel._observationStateFlow.collectAsState()

    val chartData: MutableState<  ChartDataList> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= seaWaterInfo.value) {
        if (seaWaterInfo.value.isNotEmpty()) {
            chartData.value = seaWaterInfo.value.filter {
                it.obsCode.contains("HB")
            }.toChartTripleList(
                nameSelector = { it.obsvtrNm },
                timeSelector = { it.obsrvnDt },
                timePattern = "yyyy-MM-dd HH:mm",
                primaryValueSelector = { it.wtem?.trim()?.toFloatOrNull() ?: 0f },
            )
        }

    }


    if(chartData.value.isNotEmpty()){
        ChartDataFlow(
            chartScope = ChartGraphScope.XY,
            chartData = ChartData.TimeSeries(chartData.value),
            title = "24-hour Sea Water Temperature",
            xTitle = "DateTime",
            yTitle = "Water Temperature °C",
            caption = "from https://www.data.go.kr/data/15155516/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.Line,
            yRangePadding = 1.0f,
            legendTitle = "Observatory",
            // YAxis min/max 에 함께 사용될 secondaryKey
            //    secondaryKey = "tm001",
            onRefresh = onRefresh
        )
    }

}
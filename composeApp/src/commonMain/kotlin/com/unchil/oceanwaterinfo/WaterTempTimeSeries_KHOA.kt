package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WaterTempTimeSeries_KHOA(){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaObservationViewModel = remember {
        KhoaObservationViewModel(    )
    }

    val onReload:()->Unit = {

        coroutineScope.launch {
             viewModel.onEvent(KhoaObservationViewModel.Event.Refresh)
        }
    }


    LaunchedEffect(viewModel){
        while(true){
            viewModel.onEvent(KhoaObservationViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }

    val seaWaterInfo = viewModel._observationStateFlow.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val chartData: MutableState<  ChartDataList> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= seaWaterInfo.value) {
        if (seaWaterInfo.value.isNotEmpty()) {
            chartData.value = seaWaterInfo.value
                .filter { it.obsCode.contains("HB")  }
                .toChartTripleList(
                nameSelector = { it.obsvtrNm },
                timeSelector = { it.obsrvnDt },
                timePattern = "yyyy-MM-dd HH:mm",
                primaryValueSelector = { it.wtem?.trim()?.toFloatOrNull() ?: 0f },
            )
        }

    }

    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {


    // [Reload, Tooltips, Symbol, Legend]
        val bottomBarOpt = listOf(true, true, true, true)


        ChartDataFlow(
            chartData = ChartData.TimeSeries(chartData.value),
            title = "24-hour Sea Water Temperature",
            xTitle = "DateTime",
            yTitle = "Water Temperature °C",
            caption = "from https://www.data.go.kr/data/15155516/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.Line,
            yRangePadding = 1.0f,
            legendTitle = "Observatory",
            onReload = onReload,
            bottomBarOpt = bottomBarOpt
        )

        AnimatedVisibility(isLoading){
            CircularProgressIndicator(
                color = Color.DarkGray,
            )
        }

        AnimatedVisibility(seaWaterInfo.value.isEmpty() && !isLoading ){
            NotFoundData()
        }  
        AnimatedVisibility(seaWaterInfo.value.isEmpty() && isLoading ) {
            DataLoading()
        }


} //Box


}
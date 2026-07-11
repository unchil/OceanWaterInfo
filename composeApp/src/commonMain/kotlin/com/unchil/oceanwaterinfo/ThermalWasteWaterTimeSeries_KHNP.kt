package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.unchil.oceanwaterinfo.viewmodel.KhnpThermalWasteWaterViewModel
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes


@Composable
fun ThermalWasteWaterTimeSeries_KHNP() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpThermalWasteWaterViewModel = remember { KhnpThermalWasteWaterViewModel(coroutineScope) }

    val visibleProgressIndicator = remember { mutableStateOf(false) }

    val onReload:()->Unit = {
        visibleProgressIndicator.value = true
        coroutineScope.launch {
            viewModel.onEvent(KhnpThermalWasteWaterViewModel.Event.Refresh)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.refreshEvent.collect {
            visibleProgressIndicator.value = false
        }
    }

    LaunchedEffect(viewModel){
        while(true){
            visibleProgressIndicator.value = true
            viewModel.onEvent(KhnpThermalWasteWaterViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }

    val thermalWasterWaterInfo = viewModel._khnpThermalWasteWaterStateFlow.collectAsState()


    val chartData: MutableState<  ChartDataList> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= thermalWasterWaterInfo.value){
        if(thermalWasterWaterInfo.value.isNotEmpty()){
            chartData.value = thermalWasterWaterInfo.value.filter { item ->
                val previousHour = kotlin.time.Clock.System.now()
                    .minus(24, DateTimeUnit.HOUR)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toInstant(TimeZone.UTC)

                val checkTime_Wastewater = 30.minutes

                val time = LocalDateTime.parse(item.time.replace(" ", "T")).toInstant(TimeZone.UTC)
                val rm01 = LocalDateTime.parse(item.rm001_time.replace(" ", "T")).toInstant(TimeZone.UTC)
                val rm05 = LocalDateTime.parse(item.rm005_time.replace(" ", "T")).toInstant(TimeZone.UTC)

                //          time >= previousHour &&

                (time - rm01).absoluteValue <= checkTime_Wastewater &&
                        (time - rm05).absoluteValue <= checkTime_Wastewater


                // WS 의 경우 취수 와 배수의 온도가 바뀐것 같음
            }.toChartTripleList(
                nameSelector = { it.genName },
                timeSelector = { it.time },
                timePattern = "yyyy-MM-dd HH:mm",
                primaryValueSelector = {
                    it.rm001.trim().toFloatOrNull() ?: 0f
                },
                secondaryValueSelector = {
                    it.rm005.trim().toFloatOrNull() ?: 0f
                },
                secondaryKey = "rm005"
            )
        }
    }

    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {

    // [Reload, Tooltips, Symbol, Legend]
        val bottomBarOpt = listOf(true, true, false, true)

        ChartDataFlow(
            chartData = ChartData.TimeSeries(chartData.value),
            title = "24-hour ThermalWasteWater Current",
            xTitle = "DateTime",
            yTitle = "Water Temperature(°C)",
            caption = "from https://www.data.go.kr/data/15157696/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.Area,
            yRangePadding = 1.0f,
            legendTitle = "Power Plant",
            // YAxis min/max 에 함께 사용될 secondaryKey
            secondaryKey = "rm005",
            onReload = onReload,
            bottomBarOpt = bottomBarOpt
        )



        AnimatedVisibility(visibleProgressIndicator.value){
            CircularProgressIndicator(
                color = Color.DarkGray,
            )
        }

    } //Box



}
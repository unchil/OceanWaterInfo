package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.unchil.oceanwaterinfo.ChartDataFlowTimeSeries
import com.unchil.oceanwaterinfo.ChartType
import com.unchil.oceanwaterinfo.toChartTripleList
import com.unchil.oceanwaterinfo.viewmodel.KhnpWasteWaterViewModel
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
fun WasteWaterTimeSeries_KHNP() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpWasteWaterViewModel = remember { KhnpWasteWaterViewModel(coroutineScope) }
    val wasterWaterInfo = viewModel._khnpWasteWaterStateFlow.collectAsState()
    val chartData: MutableState< ChartDataList> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= wasterWaterInfo.value){
        // ViewModel 데이터를 ChartDataList로 변환하는 로직만 수행
        chartData.value = wasterWaterInfo.value.filter { item ->
            val previousHour = kotlin.time.Clock.System.now()
                .minus(3, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toInstant(TimeZone.UTC)

            val checkTime_Wastewater = 10.minutes

            val time = LocalDateTime.parse(item.time.replace(" ", "T")).toInstant(TimeZone.UTC)
            val tm01 = LocalDateTime.parse(item.tm001_time.replace(" ", "T")).toInstant(TimeZone.UTC)
            val tm02 = LocalDateTime.parse(item.tm002_time.replace(" ", "T")).toInstant(TimeZone.UTC)

            time >= previousHour &&
                    (time - tm01).absoluteValue <= checkTime_Wastewater &&
                    (time - tm02).absoluteValue <= checkTime_Wastewater

        }.toChartTripleList(
            nameSelector = { it.genName },
            timeSelector = { it.time },
            timePattern = "yyyy-MM-dd HH:mm",
            primaryValueSelector = { it.tm002.trim().toFloatOrNull() ?: 0f },
            secondaryValueSelector = { it.tm001.trim().toFloatOrNull() ?: 0f }, // 유량
            secondaryKey = "tm001"
        )
    }



    if(chartData.value.isNotEmpty()){
        ChartDataFlowTimeSeries(
            chartData = chartData.value,
            title = "3-hour WasteWater Current",
            xTitle = "DateTime",
            yTitle = "Quality(PH)",
            caption = "from https://www.data.go.kr/data/15157700/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.Point,
            yRangePadding = 0.1f,
            // YAxis min/max 에 함께 사용될 secondaryKey
        //    secondaryKey = "tm001",
            onRefresh = {
                coroutineScope.launch {
                    while(true){
                        delay(5 * 60 * 1000L).let{
                            viewModel.onEvent(KhnpWasteWaterViewModel.Event.Refresh)
                        }
                    }
                }
            }
        )
    }




}
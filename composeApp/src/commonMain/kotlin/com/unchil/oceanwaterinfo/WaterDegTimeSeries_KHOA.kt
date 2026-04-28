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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun WaterDegTimeSeries_KHOA(){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaObservationViewModel = remember {
        KhoaObservationViewModel(  coroutineScope  )
    }
    val seaWaterInfo = viewModel._observationStateFlow.collectAsState()
    val chartData: MutableState< ChartDataList> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= seaWaterInfo.value){

        chartData.value = seaWaterInfo.value.filter {
            val previous6Hour = kotlin.time.Clock.System.now()
                .minus(6, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm")})

            it.obsCode.contains("HB") &&
                    it.obsrvnDt > previous6Hour

        }.toChartTripleList(
            nameSelector = { it.obsvtrNm },
            timeSelector = { it.obsrvnDt },
            timePattern = "yyyy-MM-dd HH:mm",
            primaryValueSelector = { it.crsp?.trim()?.toFloatOrNull() ?: 0f },
            secondaryValueSelector = { it.crdir?.trim()?.toFloatOrNull() ?: 0f },
            secondaryKey = "crdir"
        )


    }




    if(chartData.value.isNotEmpty()){
        ChartDataFlowTimeSeries(
            chartData = chartData.value,
            title = "6-hour Direction/Speed of ocean current",
            xTitle = "DateTime",
            yTitle = "Speed(cm/sec)",
            caption = "from https://www.data.go.kr/data/15155516/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.DegLine,
            yRangePadding = 1.0f,
            // YAxis min/max 에 함께 사용될 secondaryKey
          //  secondaryKey = ,
            onRefresh = {
                coroutineScope.launch {
                    while(true){
                        delay(5 * 60 * 1000L).let{
                            viewModel.onEvent(KhoaObservationViewModel.Event.Refresh)
                        }
                    }
                }
            }
        )
    }


}
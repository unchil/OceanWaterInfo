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

    val visibleProgressIndicator = remember { mutableStateOf(false) }

    val onReload:()->Unit = {
        visibleProgressIndicator.value = true
        coroutineScope.launch {
            viewModel.onEvent(KhoaObservationViewModel.Event.Refresh)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.refreshEvent.collect {
            visibleProgressIndicator.value = false
        }
    }

    LaunchedEffect(viewModel){
        while(true){
            delay(5 * 60 * 1000L).let{
                visibleProgressIndicator.value = true
                viewModel.onEvent(KhoaObservationViewModel.Event.Refresh)
            }
        }
    }

    val seaWaterInfo = viewModel._observationStateFlow.collectAsState()

    val chartData: MutableState<  ChartDataList> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= seaWaterInfo.value){
        if(seaWaterInfo.value.isNotEmpty()){
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
    }

    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {

    // [Reload, Tooltips, Symbol, Legend]
        val bottomBarOpt = listOf(true, true, true, true)

        ChartDataFlow(
            chartData = ChartData.TimeSeries(chartData.value),
            title = "6-hour Direction/Speed of ocean current",
            xTitle = "DateTime",
            yTitle = "Speed(cm/sec)",
            caption = "from https://www.data.go.kr/data/15155516/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.DegLine,
            yRangePadding = 1.0f,
            legendTitle = "Observatory",
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
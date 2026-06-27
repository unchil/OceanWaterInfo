package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhnpWasteWaterViewModel
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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WasteWaterTimeSeries_KHNP() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpWasteWaterViewModel = remember { KhnpWasteWaterViewModel(coroutineScope) }


    val visibleProgressIndicator = remember { mutableStateOf(false) }
    val onReload:()->Unit = {
        visibleProgressIndicator.value = true
        coroutineScope.launch {
            viewModel.onEvent(KhnpWasteWaterViewModel.Event.Refresh)
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
                viewModel.onEvent(KhnpWasteWaterViewModel.Event.Refresh)
            }
        }
    }

    val wasterWaterInfo = viewModel._khnpWasteWaterStateFlow.collectAsState()

    val chartData: MutableState<  ChartDataList> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= wasterWaterInfo.value){
        if(wasterWaterInfo.value.isNotEmpty()){
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

    }

    var description by remember { mutableStateOf(false) }

    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {

    // [Reload, Tooltips, Symbol, Legend]
    val bottomBarOpt = listOf(true, true, true, true)

    ChartDataFlow(
            chartData = ChartData.TimeSeries(chartData.value),
            title = "3-hour WasteWater Current",
            xTitle = "DateTime",
            yTitle = "Quality(PH)",
            caption = "from https://www.data.go.kr/data/15157700/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.Point,
            yRangePadding = 0.1f,
            legendTitle = "Power Plant",
            onReload = onReload,
            bottomBarOpt = bottomBarOpt
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val rotation by animateFloatAsState(targetValue = if (description) 180f else 0f)

                IconButton(
                    onClick = { description = !description },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(){
                        Text("Warning")
                        Icon(
                            imageVector = Icons.Default.ArrowCircleDown,
                            contentDescription = "Toggle Description",
                            modifier = Modifier.rotate(rotation) // 회전 애니메이션 적용
                        )
                    }


                }

            }

            AnimatedVisibility(description) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())

                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = WasteWaterDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                }
            }


        }

        AnimatedVisibility(visibleProgressIndicator.value){
            CircularProgressIndicator(
                color = Color.DarkGray,
            )
        }

    } //Box




}
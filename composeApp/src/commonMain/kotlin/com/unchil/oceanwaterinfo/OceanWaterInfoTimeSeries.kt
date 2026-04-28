package com.unchil.oceanwaterinfo

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
import com.unchil.oceanwaterinfo.SEA_AREA.gru_nam
import com.unchil.oceanwaterinfo.viewmodel.KhnpWasteWaterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OceanWaterInfoTimeSeries(){

    val coroutineScope = rememberCoroutineScope()

    val viewModel: NifsSeaWaterInfoViewModel = remember {
        NifsSeaWaterInfoViewModel( coroutineScope )
    }

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()
    val chartData: MutableState< ChartDataList> = remember { mutableStateOf(emptyList() ) }

    var selectedOption by remember { mutableStateOf(SEA_AREA.GRU_NAME.entries[0]) }

    val onSelection: ( SEA_AREA.GRU_NAME ) -> Unit = { entry ->
        selectedOption = entry
    }
    LaunchedEffect(key1= seaWaterInfo.value){
        chartData.value = seaWaterInfo.value.filter { it ->
            it.gru_nam.equals(selectedOption.gru_nam()) &&  it.obs_lay == "1"
        }.toChartTripleList(
            nameSelector = { it.sta_nam_kor },
            timeSelector = { it.obs_datetime },
            timePattern = "yyyy-MM-dd HH:mm:ss",
            primaryValueSelector = { it.wtr_tmp.trim().toFloatOrNull() ?: 0f },
        )
    }


    if(chartData.value.isNotEmpty()){
        ChartDataFlowTimeSeries(
            chartData = chartData.value,
            title = "24-hour Surface Sea Temperature",
            xTitle = "DateTime",
            yTitle = "Water Temperature °C",
            caption = "from https://www.nifs.go.kr (National Institute of Fisheries Science)",
            chartType = ChartType.Line,
            yRangePadding = 0.1f,
            // YAxis min/max 에 함께 사용될 secondaryKey
            //    secondaryKey = "tm001",
            onRefresh = {
                coroutineScope.launch {
                    while(true){
                        delay(1 * 60 * 1000L).let{
                            viewModel.onEvent(NifsSeaWaterInfoViewModel.Event.Refresh)
                        }
                    }
                }
            },
        ){

            var selectedTabIndex by remember { mutableIntStateOf(0) }
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
                contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
            ) {
                SEA_AREA.GRU_NAME.entries.forEachIndexed { index, entrie ->

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
    }


}
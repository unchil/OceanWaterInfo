package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.unchil.oceanwaterinfo.SEA_AREA.gru_nam
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OceanWaterInfoBarChart(){

    val coroutineScope = rememberCoroutineScope()
    val viewModel: NifsSeaWaterInfoCurrentViewModel = remember {
        NifsSeaWaterInfoCurrentViewModel(  coroutineScope  )
    }

    val visibleProgressIndicator = remember { mutableStateOf(false) }

    val onReload:()->Unit = {
        visibleProgressIndicator.value = true
        coroutineScope.launch {
            viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
        }
    }

    LaunchedEffect(visibleProgressIndicator.value){
        if(visibleProgressIndicator.value){
            delay(2000)
            visibleProgressIndicator.value = false
        }
    }

    LaunchedEffect(viewModel){
        while(true){
            delay(5 * 60 * 1000L).let{
                viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
            }
        }
    }

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()
    var selectedOption by remember { mutableStateOf(SEA_AREA.GRU_NAME.entries[0]) }
    val onSelection: ( SEA_AREA.GRU_NAME ) -> Unit = { entry ->
        selectedOption = entry
    }

    val chartData: MutableState<  ChartDataListStringFloat> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= seaWaterInfo.value,  key2=selectedOption){
        if(seaWaterInfo.value.isNotEmpty()){
            chartData.value = seaWaterInfo.value.filter {
                it.gru_nam.equals(selectedOption.gru_nam()) &&  it.obs_lay == "1"
            }.toBarChartTripleList(
                nameSelector = { it.sta_nam_kor },
                primaryValueSelector = {it.wtr_tmp.trim().toFloatOrNull() ?: 0f},
                secondaryValueSelector = { Triple(it.gru_nam, it.sta_cde, it.obs_datetime) }
            )
        }
    }

    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {

    // [Reload, Tooltips, Symbol, Legend]
    val bottomBarOpt = listOf(true, true, false, true)

    ChartDataFlow(
            chartData = ChartData.XYPlotStringFloat(chartData.value),
            title = "Surface Temperature",
            xTitle = "DateTime",
            yTitle = "Water Temperature °C",
            caption = "from https://www.nifs.go.kr (National Institute of Fisheries Science)",
            chartType = ChartType.VerticalBar,
            yRangePadding = 1.0f,
            legendTitle = "Observatory",
            onReload = onReload,
            bottomBarOpt = bottomBarOpt
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


        AnimatedVisibility(visibleProgressIndicator.value){
            CircularProgressIndicator(
                color = Color.DarkGray,
            )
        }

    } //Box




}
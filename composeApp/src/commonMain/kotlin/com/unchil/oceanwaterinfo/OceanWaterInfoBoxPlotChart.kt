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
fun OceanWaterInfoBoxPlotChart(){
    val coroutineScope = rememberCoroutineScope()

    val viewModel: NifsSeaWaterInfoViewModel = remember {
        NifsSeaWaterInfoViewModel( )
    }

    val onReload:()->Unit = {

        coroutineScope.launch {
            viewModel.onEvent(NifsSeaWaterInfoViewModel.Event.Refresh)
        }
    }




    LaunchedEffect(viewModel){
        while(true){

            viewModel.onEvent(NifsSeaWaterInfoViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    var selectedOption by remember { mutableStateOf(SEA_AREA.GRU_NAME.entries[0]) }
    val onSelection: ( SEA_AREA.GRU_NAME ) -> Unit = { entry ->
        selectedOption = entry
    }

    val chartData: MutableState< ChartDataBoxPlot> = remember { mutableStateOf(emptyMap() ) }


    LaunchedEffect(key1= seaWaterInfo.value,  key2=selectedOption){
        if(seaWaterInfo.value.isNotEmpty()){
            chartData.value = seaWaterInfo.value.filter {
                it.gru_nam.equals(selectedOption.gru_nam()) &&  it.obs_lay == "1"
            }.toBoxPlotMap()
        }
    }


    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {
    // [Reload, Tooltips, Symbol, Legend]
        val bottomBarOpt = listOf(true, true, false, true)

        ChartDataFlow(
            chartData = ChartData.XYPlotBoxPlot(chartData.value),
            title = "Surface Temperature 24-Hour Stat",
            xTitle = "Observatory",
            yTitle = "Water Temperature °C",
            caption = "from https://www.nifs.go.kr (National Institute of Fisheries Science)",
            chartType = ChartType.BoxPlot,
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

    AnimatedVisibility(isLoading){
        CircularProgressIndicator(
            color = Color.DarkGray,
        )
    }

    AnimatedVisibility(seaWaterInfo.value.isEmpty()){
        Text( "수집된 데이터가 존재하지 않습니다.", color = Color.Red, )
    }

} //Box



}
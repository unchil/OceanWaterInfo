package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.WATER_QUALITY.desc
import com.unchil.oceanwaterinfo.WATER_QUALITY.name
import com.unchil.oceanwaterinfo.WATER_QUALITY.unit
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OceanWaterInfo_MOF(){

    val coroutineScope = rememberCoroutineScope()

    val viewModel: MofSeaWaterInfoViewModel = remember {
        MofSeaWaterInfoViewModel(  )
    }


    val onReload:()->Unit = {

        coroutineScope.launch {
            viewModel.onEvent(MofSeaWaterInfoViewModel.Event.Refresh)
        }
    }


    LaunchedEffect(viewModel){
        while(true){

            viewModel.onEvent(MofSeaWaterInfoViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }

    var selectedOption by remember { mutableStateOf(WATER_QUALITY.QualityType.entries[0]) }

    val onSelection: ( WATER_QUALITY.QualityType ) -> Unit = { entry ->
        selectedOption = entry
    }

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()


    val chartData: MutableState<  ChartDataList> = remember { mutableStateOf(emptyList() ) }


    LaunchedEffect(key1= seaWaterInfo.value,  key2=selectedOption){
        if(seaWaterInfo.value.isNotEmpty()){
            chartData.value = seaWaterInfo.value.toChartTripleList(
                nameSelector = { it.rtmWqWtchStaName },
                timeSelector = { it.rtmWqWtchDtlDt },
                timePattern = "yyyy-MM-dd HH:mm:ss",
                primaryValueSelector = {
                    when (selectedOption) {
                        WATER_QUALITY.QualityType.rtmWtchWtem -> it.rtmWtchWtem
                        WATER_QUALITY.QualityType.rtmWqCndctv -> it.rtmWqCndctv
                        WATER_QUALITY.QualityType.ph -> it.ph
                        WATER_QUALITY.QualityType.rtmWqDoxn -> it.rtmWqDoxn
                        WATER_QUALITY.QualityType.rtmWqTu -> it.rtmWqTu
                        WATER_QUALITY.QualityType.rtmWqChpla -> it.rtmWqChpla
                        WATER_QUALITY.QualityType.rtmWqSlnty -> it.rtmWqSlnty
                    }.trim().toFloatOrNull() ?: 0f
                }
            )
        }
    }


    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {


    // [Reload, Tooltips, Symbol, Legend]
        val bottomBarOpt = listOf(true, true, true, true)

        ChartDataFlow(
            chartType = ChartType.Line,
            chartData = ChartData.TimeSeries(chartData.value),
            title = "24-hour Ocean Water Information",
            xTitle = "DateTime",
            yTitle =  selectedOption.unit(),
            caption = "from https://www.mof.go.kr (Ministry of Oceans and Fisheries)",
            description = selectedOption.desc(),
            yRangePadding = 0.1f,
            selectedOption = selectedOption,
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
                WATER_QUALITY.QualityType.entries.forEachIndexed { index, entrie ->

                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            onSelection(entrie)
                        },
                        text = {
                            Text(
                                text = entrie.name(),
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

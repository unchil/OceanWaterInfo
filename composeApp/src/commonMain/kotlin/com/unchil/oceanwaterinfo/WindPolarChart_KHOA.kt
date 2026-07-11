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
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationViewModel
import io.github.koalaplot.core.polar.DefaultPolarPoint
import io.github.koalaplot.core.util.deg
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WindPolarChart_KHOA(){

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
            visibleProgressIndicator.value = true
            viewModel.onEvent(KhoaObservationViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }


    val windInfo = viewModel._observationStateFlow.collectAsState()

    val chartData: MutableState<  ChartDataPolar> = remember { mutableStateOf(emptyList() ) }
    val chartTitle = remember { mutableStateOf("")}
    val maxCrSp = remember { mutableStateOf(0f)}

    LaunchedEffect(windInfo.value){

        if(windInfo.value.isNotEmpty()){

            val maxDate = windInfo.value.maxOfOrNull { it.obsrvnDt }

            chartTitle.value = (maxDate ?: "" ) + " 실시간 유향/유속 현황 (cm/s)"

            val filteredList = windInfo.value.filter{ it ->
                it.obsrvnDt.equals(maxDate)
            }

            maxCrSp.value =  filteredList.maxOfOrNull { it.crsp?.toFloat() ?: 0f} ?: 0f

            chartData.value = filteredList.map {
                Triple(
                    it.obsvtrNm,
                    Point(it.lat, it.lot),
                    listOf(DefaultPolarPoint(it.crsp?.toFloat() ?: 0f, it.crdir?.toDouble()?.deg ?: 0.deg)   )
                )
            }

        }
    }

    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {

    // [Reload, Tooltips, Symbol, Legend]
        val bottomBarOpt = listOf(true, false, false, false)

        ChartDataFlow(
            chartData = ChartData.PolarGraphPlot(chartData.value),
            title = chartTitle.value,
            xTitle = "",
            yTitle = "",
            caption = "from https://www.data.go.kr/data/15155516/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.Polar,
            legendTitle = "Observatory",
            height = 600.dp,
            maxCrSp = maxCrSp.value,
            visibleBottomBar = true,
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
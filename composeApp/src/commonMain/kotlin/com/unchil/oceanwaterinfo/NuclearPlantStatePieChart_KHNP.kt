package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.unchil.oceanwaterinfo.viewmodel.KhnpPlantStateViewModel
import com.unchil.oceanwaterinfo.viewmodel.KhnpThermalWasteWaterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NuclearPlantStatePieChart_KHNP(){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpPlantStateViewModel = remember { KhnpPlantStateViewModel(coroutineScope) }

    val onRefresh:()->Unit = {
        coroutineScope.launch {
            while(true){
                delay(5 * 60 * 1000L).let{
                    viewModel.onEvent(KhnpPlantStateViewModel.Event.Refresh)
                }
            }
        }
    }
    val plantstates = viewModel._khnpPlantState.collectAsState()
    val chartData: MutableState<  ChartDataList> = remember { mutableStateOf(emptyList() ) }

    LaunchedEffect(key1= plantstates.value){
        if(plantstates.value.isNotEmpty()){
            val aa = plantstates.value
        }
    }

}
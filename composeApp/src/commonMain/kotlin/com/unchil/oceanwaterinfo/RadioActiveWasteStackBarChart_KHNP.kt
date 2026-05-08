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
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhnpRadioActiveWasteViewModel
import com.unchil.oceanwaterinfo.viewmodel.KhnpRadioRateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KHNPRadioActiveWasteStackBarChart() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpRadioActiveWasteViewModel = remember {
        KhnpRadioActiveWasteViewModel(  coroutineScope  )
    }

    val onRefresh:()->Unit = {
        coroutineScope.launch {
            while(true){
                delay(24 * 60 * 60 * 1000L).let{
                    viewModel.onEvent(KhnpRadioActiveWasteViewModel.Event.Refresh)
                }
            }
        }
    }

    val radioActiveWasteInfo = viewModel._khnpRadioActiveWasteStateFlow.collectAsState()
    var selectedOption by remember { mutableStateOf(POWER_PLANT_AREA.POWER_PLANT.entries[0]) }
    val onSelection: ( POWER_PLANT_AREA.POWER_PLANT ) -> Unit = { entry ->
        selectedOption = entry
    }

    val chartData: MutableState< ChartDataIntLong> = remember { mutableStateOf(Triple(emptyList(), emptyList(), emptyMap())) }

    LaunchedEffect(key1= radioActiveWasteInfo.value,  key2=selectedOption){

        val filteredData =  radioActiveWasteInfo.value.filter {
            it.genName.equals(selectedOption.name)
        }.sortedBy { it.spmon }

        if(filteredData.isNotEmpty()){
            chartData.value = filteredData.toStackedBarChartTripleList(
                entrySelector = { it.year.toIntOrNull() ?: 0 },
                groupBySelect = { it.month },
                filterSelect = { it.year.toIntOrNull() ?: 0 },
                primaryValueSelector = { it.total.toLongOrNull() ?: 0L },
                secondaryValueSelector = { mapOf("plant" to it.plant, "genName" to it.genName, "spmon" to it.spmon) },
                yAxisEntrys = {listOf( "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" )}
            )

        }
    }


    ChartDataFlow(
        chartData = ChartData.XYPlotIntLong(chartData.value),
        title = "Power Plant Radio Active Waster",
        xTitle = "Year",
        yTitle = "RadioActiveWaster",
        caption = "from https://www.data.go.kr/data/15157707/openapi.do",
        height = 600.dp,
        chartType = ChartType.StackedVerticalBar,
        legendTitle = "Month",
        onRefresh = onRefresh
    ) {

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
            contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
        ) {
            POWER_PLANT_AREA.POWER_PLANT.entries.forEachIndexed { index, entrie ->

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
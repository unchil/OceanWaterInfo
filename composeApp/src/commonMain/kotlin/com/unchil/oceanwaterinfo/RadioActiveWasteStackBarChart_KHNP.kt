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
import com.unchil.oceanwaterinfo.viewmodel.KhnpRadioActiveWasteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KHNPRadioActiveWasteStackBarChart() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpRadioActiveWasteViewModel = remember {
        KhnpRadioActiveWasteViewModel(    )
    }


    val onReload:()->Unit = {
        coroutineScope.launch {
            viewModel.onEvent(KhnpRadioActiveWasteViewModel.Event.Refresh)
        }
    }

    LaunchedEffect(viewModel){
        while(true){
            viewModel.onEvent(KhnpRadioActiveWasteViewModel.Event.Refresh)
            delay(24 * 60 * 60 * 1000L)
        }
    }

    val radioActiveWasteInfo = viewModel._khnpRadioActiveWasteStateFlow.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedOption by remember { mutableStateOf(POWER_PLANT_AREA.POWER_PLANT.entries[0]) }
    val onSelection: ( POWER_PLANT_AREA.POWER_PLANT ) -> Unit = { entry ->
        selectedOption = entry
    }

    val chartData: MutableState< ChartDataStringInt> = remember { mutableStateOf(Triple(emptyList(), emptyList(), emptyMap())) }

    LaunchedEffect(key1= radioActiveWasteInfo.value,  key2=selectedOption){

        val filteredData =  radioActiveWasteInfo.value.filter {
            it.genName.equals(selectedOption.name)
        }.sortedBy { it.spmon }

        if(filteredData.isNotEmpty()){
            chartData.value = filteredData.toStackedBarChartTripleList(
                entriesSelector = { it.year},
                groupBySelect = { it.month },
                filterSelect = { it.year },
                primaryValueSelector = { it.total.toIntOrNull() ?: 0 },
                secondaryValueSelector = { mapOf("plant" to it.plant, "genName" to it.genName, "spmon" to it.spmon) },
                yAxisEntries = {listOf( "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" )}
            )

        }
    }

    // [Reload, Tooltips, Symbol, Legend]
    val bottomBarOpt = listOf(true, true, false, true)

    ChartDataFlow(
        chartData = ChartData.XYPlotStringInt(chartData.value),
        title = "Power Plant Radio Active Waste (Year)",
        xTitle = "Year",
        yTitle = "RadioActiveWaste",
        caption = "from https://www.data.go.kr/data/15157707/openapi.do",
        chartType = ChartType.StackedVerticalBar,
        legendTitle = "Month",
        onReload = onReload,
        bottomBarOpt = bottomBarOpt
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
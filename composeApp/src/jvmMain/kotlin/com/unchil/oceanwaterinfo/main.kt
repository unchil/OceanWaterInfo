package com.unchil.oceanwaterinfo


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val WaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }
val OceanWaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }

fun main() = application {

    var initialized by remember { mutableStateOf(false) }
    var download by remember { mutableStateOf(-1) }
    var errorMessage by remember {mutableStateOf("")}

    val isReloadWaterInfoGeoChart_KHOA_MapScreen = remember { mutableStateOf(false) }

    val isRelaodOceanWaterInfoGeoChart_MapScreen  = remember { mutableStateOf(false) }


    val visibleProgressIndicatorWaterInfoGeoChart_KHOA_MapScreen = remember { mutableStateOf(false) }
    val visibleProgressIndicatorOceanWaterInfoGeoChart_MapScreen = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            KCEF.init(
                builder = {
                    progress {
                        onInitialized {
                            initialized = true
                        }
                        onDownloading {
                            download = it.toInt()
                        }
                    }
                },
                onError = {
                    errorMessage = it?.printStackTrace().toString()
                },

            )
        }
    }


    val state = WindowState(
        size = DpSize(1400.dp, 1000.dp),
        position = WindowPosition(Alignment.Center)
    )

    val initCenterPoint = remember{ Point(126.934515, 37.385852) }

    val clickPointWaterInfoGeoChart_KHOA = mutableStateOf(initCenterPoint )

    val onClickPointWaterInfoGeoChart_KHOA = { point:Point<Double, Double> ->
        clickPointWaterInfoGeoChart_KHOA.value = point
    }

    val clickPointOceanWaterInfoGeoChart = mutableStateOf(initCenterPoint )

    val onClickPointOceanWaterInfoGeoChart = { point:Point<Double, Double> ->
        clickPointOceanWaterInfoGeoChart.value = point
    }

    var selectedTabIndex by remember { mutableStateOf(0) } // 탭 인덱스 상태
    val tabTitles = listOf("Seoul/Gyonggi  Air Quality", "Korea Ocean Water")

    val coroutineScope = rememberCoroutineScope()



    Window(
        onCloseRequest = ::exitApplication,
        title = "Environmental Observation Information",
        state = state,
    ) {
        MaterialTheme(colorScheme = getColorScheme(false)) {

            CompositionLocalProvider(LocalPlatform provides getPlatform()) {

    //            OceanWaterInfo()

                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surface)
                ) {
                    SecondaryTabRow(
                        selectedTabIndex,
                        Modifier.fillMaxWidth(),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary,
                         { HorizontalDivider() }
                    ) {
                        tabTitles.forEachIndexed { index, title ->


                            val interactionSource = remember { MutableInteractionSource() }
                            // InteractionSource의 상태 변화를 직접 감지하는 로직
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collectLatest { interaction ->
                                    when (interaction) {
                                        is PressInteraction.Press -> {
                                            if (selectedTabIndex != index) {
                                                selectedTabIndex = index
                                            }
                                        }
                                    }
                                }
                            }

                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    // 고수준 onClick도 유지하되, 위 LaunchedEffect가 보조 역할을 수행합니다.
                                    if (selectedTabIndex != index) {
                                        selectedTabIndex = index
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 16.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Light,
                                        // 리사이즈 도중 텍스트가 잘려나가는 것을 방지
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                },
                                // interactionSource를 명시적으로 관리하면 시스템 부하 상황에서 더 잘 반응함
                                interactionSource = interactionSource
                            )
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedTabIndex) {
                            0 -> {
                                // --- 탭 1: Seoul SDoT 정보 ---
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        "Seoul/Gyonggi SDoT Air Environmental Observation Information",
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                        SDoTEnvInfoUnionMapHexagonLayer(initialized, download, errorMessage)

                                }
                            }
                            1 -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        "Korea Ocean Water Information",
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    var splitFractionVertical by remember { mutableStateOf(0.5f) }
                                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                        val totalWidth = constraints.maxWidth.toFloat()
                                        Row(modifier = Modifier.fillMaxSize()) {


                                            var splitFractionHorizontal by remember {
                                                mutableStateOf(
                                                    0.5f
                                                )
                                            }
                                            BoxWithConstraints(
                                                modifier = Modifier.fillMaxWidth(
                                                    splitFractionVertical
                                                ).fillMaxHeight()
                                            ) {
                                                val totalHeight = this.maxHeight.value

                                                    Column {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .fillMaxHeight(
                                                                    splitFractionHorizontal
                                                                )
                                                        ) {
                                                            SeaFlowMapTripsLayer(
                                                                initialized,
                                                                download,
                                                                errorMessage
                                                            )
                                                        }

                                                        DraggableHorizontalDivider(
                                                            onDrag = { deltaPx ->
                                                                val deltaWeight =
                                                                    deltaPx / totalHeight
                                                                splitFractionHorizontal =
                                                                    (splitFractionHorizontal + deltaWeight).coerceIn(
                                                                        0.1f,
                                                                        0.9f
                                                                    )
                                                            }
                                                        )


                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .fillMaxHeight()
                                                        ) {
                                                            SeaFlowMapHexagonLayer(
                                                                initialized,
                                                                download,
                                                                errorMessage
                                                            )
                                                        }
                                                    }

                                            }


                                            DraggableVerticalDivider(
                                                onDrag = { deltaPx ->
                                                    val deltaWeight = deltaPx / totalWidth
                                                    splitFractionVertical =
                                                        (splitFractionVertical + deltaWeight).coerceIn(
                                                            0.1f,
                                                            0.9f
                                                        )
                                                }
                                            )

                                            var splitFractionVertical2 by remember {
                                                mutableStateOf(
                                                    0.5f
                                                )
                                            }
                                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                //totalWidth2는 BoxWithConstraints 바로 아래에서 계산하는 것이 맞지만, Row가 Column에 의해 좌우 패딩을 받는다면 그 값만큼 보정해야 정확한 드래그가 가능합니다.
                                                val totalWidth2 =
                                                    with(LocalDensity.current) { maxWidth.toPx() }

                                                Column(
                                                    modifier = Modifier.fillMaxSize()
                                                        .verticalScroll(rememberScrollState()),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                ) {
                                                    NuclearPlantStatePieChart_KHNP()
                                                    RadioActiveWastePlantStatStackedBarChart_KHNP()
                                                    KHNPRadioActiveWasteStackBarChart()
                                                    RadioRateBarChart()
                                                    WasteWaterTimeSeries_KHNP()
                                                    ThermalWasteWaterTimeSeries_KHNP()
                                                    OceanWaterInfo_MOF()
                                                    WaterTempTimeSeries_KHOA()
                                                    OceanWaterInfoTimeSeries()
                                                    OceanWaterInfoBoxPlotChart()
                                                    OceanWaterInfoBarChart()
                                                    WaterDegTimeSeries_KHOA()
                                                    WindPolarChart_KHOA()

                                                    //스크롤이 가능한 Column 내부에 구분선(Divider)이 있는 레이아웃을 넣으려면, 해당 Row에 명시적인 높이(height)를 지정해야 합니다.
                                                    // 그리고 그 값은 WaterInfoGeoChart_KHOA  의 height 값 보다 커야됨
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().height(700.dp).border(BorderStroke(1.dp, Color.LightGray)).padding(6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth(splitFractionVertical2)
                                                                .fillMaxHeight(),
                                                            contentAlignment= Alignment.Center
                                                        ) {
                                                            WaterInfoGeoChart_KHOA(onClickPointWaterInfoGeoChart_KHOA)
                                                        }

                                                        DraggableVerticalDivider(
                                                            onDrag = { deltaPx ->
                                                                val deltaWeight2 =
                                                                    deltaPx / totalWidth2
                                                                splitFractionVertical2 =
                                                                    (splitFractionVertical2 + deltaWeight2).coerceIn(
                                                                        0.1f,
                                                                        0.9f
                                                                    )
                                                            }
                                                        )

                                                        Column(
                                                            modifier=Modifier.fillMaxSize(),
                                                            verticalArrangement = Arrangement.Center,
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {


                                                            CompositionLocalProvider(
                                                                WaterInfoGeoChartPoint provides clickPointWaterInfoGeoChart_KHOA.value
                                                            ) {

                                                                //WaterInfoGeoChart_KHOA_MapScreen 은 항상 height 값이 fix 되어야 표시됨.
                                                                Box(
                                                                    modifier = Modifier.fillMaxWidth().height(600.dp)
                                                                        .padding(12.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {

                                                                    WaterInfoGeoChart_KHOA_MapScreen(
                                                                        initialized,
                                                                        download,
                                                                        errorMessage,
                                                                        isReloadWaterInfoGeoChart_KHOA_MapScreen.value
                                                                    )

                                                                }
                                                            }

                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center,
                                                            ){
                                                                // [Reload, Tooltips, Symbol, Legend]
                                                                val bottomBarOpt = listOf(true, false, false, false)
                                                                ChartFeatureControls(
                                                                    onChangeFlag = { label, value ->
                                                                        when(label){
                                                                            "Reload" -> {
                                                                                clickPointWaterInfoGeoChart_KHOA.value = initCenterPoint
                                                                                isReloadWaterInfoGeoChart_KHOA_MapScreen.value = !isReloadWaterInfoGeoChart_KHOA_MapScreen.value
                                                                                visibleProgressIndicatorWaterInfoGeoChart_KHOA_MapScreen.value = true
                                                                                coroutineScope.launch {
                                                                                    delay(2000)
                                                                                    visibleProgressIndicatorWaterInfoGeoChart_KHOA_MapScreen.value = false
                                                                                }

                                                                            }
                                                                        }
                                                                    },
                                                                    bottomBarOpt = bottomBarOpt
                                                                )


                                                                if(visibleProgressIndicatorWaterInfoGeoChart_KHOA_MapScreen.value){
                                                                    CircularProgressIndicator(
                                                                        color = Color.DarkGray,
                                                                    )
                                                                }

                                                            }

                                                        }

                                                    }



                                                    Row(   modifier = Modifier.fillMaxSize().height(700.dp).border(BorderStroke(1.dp, Color.LightGray)).padding(6.dp) ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth(splitFractionVertical2)
                                                                .fillMaxHeight(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            OceanWaterInfoGeoChart(
                                                                onClickPointOceanWaterInfoGeoChart
                                                            )
                                                        }


                                                        DraggableVerticalDivider(
                                                            onDrag = { deltaPx ->
                                                                val deltaWeight2 =
                                                                    deltaPx / totalWidth2
                                                                splitFractionVertical2 =
                                                                    (splitFractionVertical2 + deltaWeight2).coerceIn(
                                                                        0.1f,
                                                                        0.9f
                                                                    )
                                                            }
                                                        )


                                                        Column(
                                                            modifier = Modifier.fillMaxSize(),
                                                            verticalArrangement = Arrangement.Center,
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {


                                                            CompositionLocalProvider(
                                                                OceanWaterInfoGeoChartPoint provides clickPointOceanWaterInfoGeoChart.value
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth().height(600.dp).padding(12.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {

                                                                    OceanWaterInfoGeoChart_MapScreen(
                                                                        initialized,
                                                                        download,
                                                                        errorMessage,
                                                                        isRelaodOceanWaterInfoGeoChart_MapScreen.value
                                                                    )


                                                                }
                                                            }


                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center,
                                                            ){
                                                                // [Reload, Tooltips, Symbol, Legend]
                                                                val bottomBarOpt = listOf(true, false, false, false)
                                                                ChartFeatureControls(
                                                                    onChangeFlag = { label, value ->
                                                                        when(label){
                                                                            "Reload" -> {
                                                                                clickPointOceanWaterInfoGeoChart.value = initCenterPoint
                                                                                isRelaodOceanWaterInfoGeoChart_MapScreen.value = !isRelaodOceanWaterInfoGeoChart_MapScreen.value

                                                                                visibleProgressIndicatorOceanWaterInfoGeoChart_MapScreen.value = true
                                                                                coroutineScope.launch {
                                                                                    delay(2000)
                                                                                    visibleProgressIndicatorOceanWaterInfoGeoChart_MapScreen.value = false
                                                                                }

                                                                            }
                                                                        }
                                                                    },
                                                                    bottomBarOpt = bottomBarOpt
                                                                )


                                                                if(visibleProgressIndicatorOceanWaterInfoGeoChart_MapScreen.value){
                                                                    CircularProgressIndicator(
                                                                        color = Color.DarkGray,
                                                                    )
                                                                }

                                                            }

                                                        }





                                                    }


                                                  OceanWaterInfoDataGrid()
                                                }

                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }






            }
        }
    }



    DisposableEffect(Unit) {
        onDispose {
            KCEF.disposeBlocking()
        }
    }








}


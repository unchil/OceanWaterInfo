package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import com.unchil.oceanwaterinfo.AirQualityManager.nameEn
import com.unchil.oceanwaterinfo.viewmodel.KhoaTidalCurrentViewModel
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoUnionViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main(){

    val mainHtmlElementId = "webmain"
    val airInfoMapHtmlElementId = "airInfoMap"
    val waterInfoMapHtmlElementId = "waterInfoMap"
    val oceanWaterInfoMapHtmlElementId = "oceanWaterInfoMap"

    val seaFlowHexagonMapHtmlElementId = "seaFlowHexagonMap"
    val seaFlowTripsMapMapHtmlElementId = "seaFlowTripsMap"


    ComposeViewport(viewportContainerId = mainHtmlElementId) {

        val mapScreenHeight = remember{700.dp}
        val bottomBarHeight = remember{100.dp}

        val initCenterPoint = remember{ Point(126.934515, 37.385852) }
        val isReload = remember { mutableStateOf(false) }
        val isReloadWaterInfoMap = remember { mutableStateOf(0) }
        val isReloadOceanWaterInfoMap = remember { mutableStateOf(0) }



        var selectedTabIndex by remember { mutableStateOf(0) } // 탭 인덱스 상태
        val tabTitles = listOf("Seoul/Gyonggi Air Quality", "Korea Ocean Water Quality", "Korea Tidal Forecast Map", "Korean Ocean Current Speed Map", "Korea Hydro & Nuclear Power")


        val density = LocalDensity.current
        val coroutineScope = rememberCoroutineScope()
        var descriptionBox by remember { mutableStateOf(false) }


        LaunchedEffect(selectedTabIndex) {
            changeSelectedTab(selectedTabIndex)
        }


        MaterialTheme(
            typography = getTypography(),
            colorScheme = getColorScheme(false))
        {

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

                            val initData = remember{ mutableStateOf("" )}
                            val visibleProgressIndicator = remember { mutableStateOf(false) }

                            var selectedOption by remember { mutableStateOf(AirQualityManager.ChemicalElement.entries[0]) }

                            val viewModelSDoTEnvInfoUnion: SDoTEnvInfoUnionViewModel = remember {
                                SDoTEnvInfoUnionViewModel()
                            }
                            LaunchedEffect(key1 = viewModelSDoTEnvInfoUnion){
                                while(true){
                                    viewModelSDoTEnvInfoUnion.onEvent(SDoTEnvInfoUnionViewModel.Event.Refresh)
                                    delay(5 * 60 * 1000L)
                                }
                            }

                            val sDoTEnvInfo = viewModelSDoTEnvInfoUnion._sDoTEnvInfoUnionFlow.collectAsState()

                            LaunchedEffect( sDoTEnvInfo.value, key2=selectedOption){

                                if(sDoTEnvInfo.value.isNotEmpty()) {
                                    initData.value = sDoTEnvInfo.value.map{it}.joinToString(
                                        separator = ",",
                                        prefix = "[",
                                        postfix = "]"
                                    ) { it ->
                                        val value = when (selectedOption) {
                                            AirQualityManager.ChemicalElement.o3 -> it.o3.toFloatOrNull() ?: 0f
                                            AirQualityManager.ChemicalElement.no2 -> it.no2.toFloatOrNull() ?: 0f
                                            AirQualityManager.ChemicalElement.co -> it.co.toFloatOrNull() ?: 0f
                                            AirQualityManager.ChemicalElement.so2 -> it.so2.toFloatOrNull() ?: 0f
                                            AirQualityManager.ChemicalElement.nh3 -> it.nh3.toFloatOrNull() ?: 0f
                                            AirQualityManager.ChemicalElement.h2s -> it.h2s.toFloatOrNull() ?: 0f
                                            AirQualityManager.ChemicalElement.pm10 -> it.pm10.toFloatOrNull() ?: 0f
                                            AirQualityManager.ChemicalElement.pm25 -> it.pm25.toFloatOrNull() ?: 0f
                                        }
                                        "{ \"sensing_time\":\"${it.sensing_time}\", \"obs\":\"${it.obs}\", \"lat\":${it.lat}, \"lng\":${it.lng},  \"addr\":\"${it.addr}\", \"value\":${value} }"
                                    }

                                    onClickTabPositionAirInfo2.invoke(initData.value, selectedOption.name)

                                }


                            }

                            Column (modifier= Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {


                                Text(
                                    "Seoul/Gyonggi SDoT Air Environmental Observation Information",
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(vertical = 15.dp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                var selectedTabIndexSub by remember { mutableIntStateOf(0) }

                                SecondaryTabRow(
                                    selectedTabIndex = selectedTabIndexSub,
                                    containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
                                    contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
                                ) {
                                    AirQualityManager.ChemicalElement.entries.forEachIndexed { index, element ->
                                        Tab(
                                            selected = selectedTabIndexSub == index,
                                            onClick = {
                                                selectedTabIndexSub = index
                                                selectedOption = element
                                            },
                                            text = {
                                                Text(
                                                    text = element.nameEn(),
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                            }
                                        )
                                    }
                                }




                                    BoxWithConstraints(
                                        modifier = Modifier.fillMaxSize()
                                    ){
                                        val totalWidth = constraints.maxWidth.toFloat()
                                        val height = this.maxHeight - bottomBarHeight


                                        Column(
                                            modifier= Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Top,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {

                                            Row(modifier = Modifier.fillMaxWidth().height(height)) {

                                                var splitFractionVertical by remember { mutableStateOf(0.3f) }

                                                AnimatedVisibility(descriptionBox){
                                                    SDoTDescription(sDoTEnvInfo = sDoTEnvInfo.value, selectedOption= selectedOption, splitFractionVertical = splitFractionVertical)
                                                }

                                                Box( modifier = Modifier.width(24.dp).fillMaxHeight()
                                                    ,contentAlignment = Alignment.Center ){

                                                    val rotation by animateFloatAsState(targetValue = if (descriptionBox) 180f else 0f)

                                                    IconButton( onClick = { descriptionBox = !descriptionBox }
                                                        ,modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon( imageVector = Icons.Default.ArrowCircleRight
                                                            ,contentDescription = "Toggle Description"
                                                            ,modifier = Modifier.rotate(rotation)
                                                        )
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




                                                Box(
                                                    modifier = Modifier.fillMaxWidth()
                                                        .height(height )
                                                        .onGloballyPositioned { coordinates ->
                                                            syncHtmlElementPosition(
                                                                coordinates,
                                                                density,
                                                                mainHtmlElementId,
                                                                airInfoMapHtmlElementId
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.

                                                }


                                            }


                                            Box(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {// [Reload, Tooltips, Symbol, Legend]
                                                val bottomBarOpt =
                                                    listOf(true, false, false, false)
                                                ChartFeatureControls(
                                                    onChangeFlag = { label, value ->
                                                        when (label) {
                                                            "Reload" ->{
                                                                visibleProgressIndicator.value = true
                                                                onClickTabPositionAirInfo2.invoke(initData.value, selectedOption.name)
                                                                coroutineScope.launch {
                                                                    delay(1000)
                                                                    visibleProgressIndicator.value = false
                                                                }
                                                            }
                                                        }

                                                    },
                                                    bottomBarOpt = bottomBarOpt
                                                )
                                                if (visibleProgressIndicator.value) {
                                                    CircularProgressIndicator(
                                                        color = Color.DarkGray,
                                                    )
                                                }
                                            }


                                        }







                                    }









                            }














                        }


                        1 -> {

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                Text(
                                    "Korea Ocean Water Information",
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(vertical = 15.dp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val totalWidth = this.maxWidth.value

                                    Column(
                                        modifier = Modifier.fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .background(color = MaterialTheme.colorScheme.surface),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        OceanWaterInfo_MOF()
                                        OceanWaterInfoTimeSeries()
                                        OceanWaterInfoBoxPlotChart()
                                        OceanWaterInfoBarChart()
                                        OceanWaterInfoDataGrid()

                                        var splitFractionVertical by remember {
                                            mutableStateOf(
                                                0.5f
                                            )
                                        }
                                        val mapScreenHeight = remember{700.dp}

                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(mapScreenHeight).border(BorderStroke(1.dp, Color.LightGray)).padding(6.dp)
                                        ) {

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(splitFractionVertical)
                                                    .fillMaxHeight(),
                                                contentAlignment= Alignment.Center
                                            ) {
                                                OceanWaterInfoGeoChart(
                                                    onClickPoint = sendFlyToTargetOceanWater,
                                                    sendAddMarkerClusterer = sendAddMarkerClusterer,
                                                    onClickPointOceanWaterInfoGeoChart = sendFlyToTargetOceanWater,
                                                    isReload = isReloadOceanWaterInfoMap.value
                                                )
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



                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalArrangement = Arrangement.Top,
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                ) {


                                                    Box(
                                                        modifier = Modifier.fillMaxWidth()
                                                            .height(mapScreenHeight - bottomBarHeight)
                                                            .onGloballyPositioned { coordinates ->
                                                                syncHtmlElementPosition(
                                                                    coordinates,
                                                                    density,
                                                                    mainHtmlElementId,
                                                                    oceanWaterInfoMapHtmlElementId
                                                                )
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                                                    }


                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center,
                                                    ) {// [Reload, Tooltips, Symbol, Legend]
                                                        val bottomBarOpt =
                                                            listOf(true, false, false, false)
                                                        ChartFeatureControls(
                                                            onChangeFlag = { label, value ->
                                                                when (label) {
                                                                    "Reload" -> isReloadOceanWaterInfoMap.value = kotlin.time.Clock.System.now().nanosecondsOfSecond
                                                                }
                                                            },
                                                            bottomBarOpt = bottomBarOpt
                                                        )

                                                    }


                                                }



                                        }
                                    }

                                }
                            }

                        }

                        2 -> {

                            val initData = remember{ mutableStateOf("" )}
                            val visibleProgressIndicator = remember { mutableStateOf(false) }

                            val viewModel: KhoaTidalCurrentViewModel = remember {
                                KhoaTidalCurrentViewModel()
                            }


                            LaunchedEffect(key1 = viewModel){
                                while(true){
                                    viewModel.onEvent(KhoaTidalCurrentViewModel.Event.Refresh)
                                    delay(5 * 60 * 1000L)
                                }
                            }

                            val tidalCurrentInfo = viewModel._tidalCurrentStateFlow.collectAsState()

                            LaunchedEffect( tidalCurrentInfo.value){


                                if(tidalCurrentInfo.value.isNotEmpty()) {
                                    val tidalCurrentData = tidalCurrentInfo.value.toTidalCurrentDataMap()
                                    updatePrevCoordinates(tidalCurrentData)
                                    initData.value = tidalCurrentData.map{it}.joinToString(
                                        separator = ",",
                                        prefix = "[",
                                        postfix = "]"
                                    ){ it ->
                                        val data = it.value.joinToString (
                                            separator = ",",
                                            prefix = "[",
                                            postfix = "]"
                                        ){ (schTime, currentDir, currentSpeed,  prev_lon, prev_lat) ->
                                            "{\"lat\":${prev_lat}, \"lng\":${prev_lon}, \"speed\":${currentSpeed}}"
                                        }
                                        data
                                    }

                                    onInitData( IFRAME_SEA_FLOW_TRIPS, initData.value)
                                }
                            }


                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val height = this.maxHeight

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {

                                    ChartTitle(
                                        "Prediction 3 hour The Tidal Current Map",
                                        modifier = Modifier,
                                    )

                                    CaptionText(
                                        "from https://khoa.go.kr/oceandata/api/tidalCurrentArea/search.do (Korea Hydrographic And Oceanographic Agency)",
                                        textAlign = TextAlign.Center
                                    )

                                    Box(
                                        modifier = Modifier.fillMaxWidth()
                                            .height(height - bottomBarHeight)
                                            .onGloballyPositioned { coordinates ->
                                                syncHtmlElementPosition(
                                                    coordinates,
                                                    density,
                                                    mainHtmlElementId,
                                                    DIV_SEA_FLOW_TRIPS
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                                    }



                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {// [Reload, Tooltips, Symbol, Legend]
                                        val bottomBarOpt =
                                            listOf(true, false, false, false)
                                        ChartFeatureControls(
                                            onChangeFlag = { label, value ->
                                                when (label) {
                                                    "Reload" ->{
                                                        visibleProgressIndicator.value = true
                                                        onInitData( IFRAME_SEA_FLOW_TRIPS, initData.value)
                                                        coroutineScope.launch {
                                                            delay(1000)
                                                            visibleProgressIndicator.value = false
                                                        }
                                                    }
                                                }

                                            },
                                            bottomBarOpt = bottomBarOpt
                                        )
                                        if (visibleProgressIndicator.value) {
                                            CircularProgressIndicator(
                                                color = Color.DarkGray,
                                            )
                                        }
                                    }


                                }

                            }
                        }

                        3 -> {

                            val initData = remember{ mutableStateOf("" )}
                            val visibleProgressIndicator = remember { mutableStateOf(false) }

                            val viewModel: KhoaTidalCurrentViewModel = remember {
                                KhoaTidalCurrentViewModel()
                            }


                            LaunchedEffect(key1 = viewModel){
                                while(true){
                                    viewModel.onEvent(KhoaTidalCurrentViewModel.Event.Refresh)
                                    delay(5 * 60 * 1000L)
                                }
                            }

                            val tidalCurrentInfo = viewModel._tidalCurrentStateFlow.collectAsState()

                            LaunchedEffect( tidalCurrentInfo.value){

                                if(tidalCurrentInfo.value.isNotEmpty()) {
                                    val tidalCurrentData = tidalCurrentInfo.value.toTidalCurrentDataMap()
                                    val data =  transformToHexagonData(tidalCurrentData)

                                    initData.value = data.map{it}.joinToString(
                                        separator = ",",
                                        prefix = "[",
                                        postfix = "]"
                                    ){ it ->
                                        //Triple(lat,lng,speed)
                                        "{\"lat\":${it.first}, \"lng\":${it.second},  \"speed\":${it.third}}"
                                    }
                                    onInitData( IFRAME_SEA_FLOW_HEXAGON, initData.value)
                                }
                            }

                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val height = this.maxHeight

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {

                                    ChartTitle(
                                        "Ocean Water Speed",
                                        modifier = Modifier,
                                    )

                                    CaptionText(
                                        "from https://khoa.go.kr/oceandata/api/tidalCurrentArea/search.do (Korea Hydrographic And Oceanographic Agency)",
                                        textAlign = TextAlign.Center
                                    )

                                    Box(
                                        modifier = Modifier.fillMaxWidth()
                                            .height(height - bottomBarHeight)
                                            .onGloballyPositioned { coordinates ->
                                                syncHtmlElementPosition(
                                                    coordinates,
                                                    density,
                                                    mainHtmlElementId,
                                                    DIV_SEA_FLOW_HEXAGON
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                                    }



                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {// [Reload, Tooltips, Symbol, Legend]
                                        val bottomBarOpt =
                                            listOf(true, false, false, false)
                                        ChartFeatureControls(
                                            onChangeFlag = { label, value ->
                                                when (label) {
                                                    "Reload" ->{
                                                        visibleProgressIndicator.value = true
                                                        onInitData( IFRAME_SEA_FLOW_HEXAGON, initData.value)
                                                        coroutineScope.launch {
                                                            delay(1000)
                                                            visibleProgressIndicator.value = false
                                                        }
                                                    }
                                                }

                                            },
                                            bottomBarOpt = bottomBarOpt
                                        )
                                        if (visibleProgressIndicator.value) {
                                            CircularProgressIndicator(
                                                color = Color.DarkGray,
                                            )
                                        }
                                    }


                                }

                            }

                        }

                        4 -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                Text(
                                    "Information regarding Korea Hydro & Nuclear Power",
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

                                    val totalWidth = this.maxWidth.value

                                    Column(
                                        modifier = Modifier.fillMaxSize()
                                            .verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {


                                        NuclearPlantStatePieChart_KHNP()
                                        RadioActiveWastePlantStatStackedBarChart_KHNP()
                                        KHNPRadioActiveWasteStackBarChart()
                                        WaterTempTimeSeries_KHOA()
                                        RadioRateBarChart()
                                        WasteWaterTimeSeries_KHNP()
                                        ThermalWasteWaterTimeSeries_KHNP()



                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .height(mapScreenHeight)
                                                .border(BorderStroke(1.dp, Color.LightGray))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {

                                            Box(
                                                modifier = Modifier.fillMaxWidth(0.3f)
                                                    .fillMaxHeight(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                WindPolarChart_KHOA()
                                            }

                                            Box(
                                                modifier = Modifier.fillMaxWidth(0.5f)
                                                    .fillMaxHeight(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                WaterInfoGeoChart_KHOA(
                                                    onClickPoint = sendFlyToTargetWater,
                                                    sendAddMarkerClusterer = sendAddMarkerClusterer,
                                                    onClickPointOceanWaterInfoGeoChart = sendFlyToTargetWater,
                                                    isReload = isReloadWaterInfoMap.value
                                                )
                                            }



                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalArrangement = Arrangement.Top,
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                ) {


                                                    Box(
                                                        modifier = Modifier.fillMaxWidth()
                                                            .height(mapScreenHeight - bottomBarHeight)
                                                            .onGloballyPositioned { coordinates ->
                                                                syncHtmlElementPosition(
                                                                    coordinates,
                                                                    density,
                                                                    mainHtmlElementId,
                                                                    waterInfoMapHtmlElementId
                                                                )
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                                                    }


                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center,
                                                    ) {// [Reload, Tooltips, Symbol, Legend]
                                                        val bottomBarOpt =
                                                            listOf(true, false, false, false)
                                                        ChartFeatureControls(
                                                            onChangeFlag = { label, value ->
                                                                when (label) {
                                                                    "Reload" -> isReloadWaterInfoMap.value = kotlin.time.Clock.System.now().nanosecondsOfSecond
                                                                }

                                                            },
                                                            bottomBarOpt = bottomBarOpt
                                                        )

                                                    }


                                                }


                                        }




                                        WaterDegTimeSeries_KHOA()
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
                disposeHtmlElements(listOf(airInfoMapHtmlElementId,waterInfoMapHtmlElementId))
            }
        }

    }
}
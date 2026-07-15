package com.unchil.oceanwaterinfo


/*

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun mainOld() {

    val mainHtmlElementId = "webmain"
    val waterInfoMapHtmlElementId = "waterInfoMap"



    ComposeViewport(viewportContainerId = mainHtmlElementId) {

        val mapScreenHeight = remember{700.dp}
        val bottomBarHeight = remember{100.dp}

        val initCenterPoint = remember{ Point(126.934515, 37.385852) }
        val isReload = remember { mutableStateOf(false) }
        val visibleProgressIndicator = remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()

        val viewModel: KhoaObservationCurrentViewModel = remember {
            KhoaObservationCurrentViewModel(coroutineScope)
        }

        LaunchedEffect(key1 = viewModel){
            viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
        }
        val seaWaterInfo = viewModel._observationStateFlow.collectAsState()

        LaunchedEffect( seaWaterInfo.value){
            if (seaWaterInfo.value.isNotEmpty()) {
                sendAddMarkerClusterer(transformToMarkerData(seaWaterInfo.value))
            }
        }

        LaunchedEffect(isReload.value){
            if(isReload.value){
                viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
                visibleProgressIndicator.value = true
                delay(1000)
                visibleProgressIndicator.value = false
                isReload.value = false
                onClickPointOceanWaterInfoGeoChart(initCenterPoint)

            }
        }

        CompositionLocalProvider(LocalPlatform provides getPlatform()) {
            val density = LocalDensity.current

            MaterialTheme(
                typography = getTypography(),
                colorScheme = getColorScheme(false)
            ) {
                Column(modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                        .background(color = MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    NuclearPlantStatePieChart_KHNP()
                    RadioActiveWastePlantStatStackedBarChart_KHNP()
                    KHNPRadioActiveWasteStackBarChart()

                    Row(
                        modifier = Modifier.fillMaxWidth().height(mapScreenHeight)
                            .border(BorderStroke(1.dp, Color.LightGray)).padding(10.dp)
                        ,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Box(
                            modifier = Modifier.fillMaxWidth(0.3f).fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ){
                            WindPolarChart_KHOA()
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ){
                            WaterInfoGeoChart_KHOA(onClickPointOceanWaterInfoGeoChart)
                        }

                        Column (modifier= Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ){
                            Box(
                                modifier = Modifier.fillMaxWidth().height( mapScreenHeight - bottomBarHeight )
                                    .onGloballyPositioned { coordinates ->
                                        syncHtmlElementPosition(coordinates, density, mainHtmlElementId, waterInfoMapHtmlElementId)
                                    },
                                contentAlignment =Alignment.Center
                            ) {
                                // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                            }



                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ){// [Reload, Tooltips, Symbol, Legend]
                                val bottomBarOpt = listOf(true, false, false, false)
                                ChartFeatureControls(
                                    onChangeFlag = { label, value ->
                                        when(label){
                                            "Reload" ->  isReload.value = !isReload.value
                                        }
                                    },
                                    bottomBarOpt = bottomBarOpt
                                )
                                if(visibleProgressIndicator.value){
                                    CircularProgressIndicator(
                                        color = Color.DarkGray,
                                    )
                                }
                            }



                        }

                    }

                    RadioRateBarChart()
                    WasteWaterTimeSeries_KHNP()
                    ThermalWasteWaterTimeSeries_KHNP()
                    OceanWaterInfo_MOF()
                    WaterTempTimeSeries_KHOA()
                    OceanWaterInfoTimeSeries()
                    OceanWaterInfoBoxPlotChart()
                    OceanWaterInfoBarChart()

                    OceanWaterInfoDataGrid()
                    WaterDegTimeSeries_KHOA()

                }

            }

        } //CompositionLocalProvider


        DisposableEffect(Unit) {
            onDispose {
                disposeHtmlElements(listOf(waterInfoMapHtmlElementId))
            }
        }

    }// ComposeViewport
}


 */
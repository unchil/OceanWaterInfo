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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
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
import com.unchil.oceanwaterinfo.AirQualityManager.airQualityStageComment
import com.unchil.oceanwaterinfo.AirQualityManager.airQualityStageRange
import com.unchil.oceanwaterinfo.AirQualityManager.nameEn
import com.unchil.oceanwaterinfo.AirQualityManager.specialFeature
import com.unchil.oceanwaterinfo.AirQualityManager.unHealthyForSensitiveGroups
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationCurrentViewModel
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoUnionViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main(){

    val mainHtmlElementId = "webmain"
    val airInfoMapHtmlElementId = "airInfoMap"
    val waterInfoMapHtmlElementId = "waterInfoMap"

    ComposeViewport(viewportContainerId = mainHtmlElementId) {

        val mapScreenHeight = remember{700.dp}
        val bottomBarHeight = remember{100.dp}

        val initCenterPoint = remember{ Point(126.934515, 37.385852) }
        val isReload = remember { mutableStateOf(false) }
        val visibleProgressIndicator = remember { mutableStateOf(false) }

        var selectedTabIndex by remember { mutableStateOf(0) } // 탭 인덱스 상태
        val tabTitles = listOf("Seoul/Gyonggi  Air Quality", "Korea Ocean Water Quality")
        var selectedOption by remember { mutableStateOf(AirQualityManager.ChemicalElement.entries[0]) }

        val density = LocalDensity.current

        val coroutineScope = rememberCoroutineScope()

        val viewModelSDoTEnvInfo: SDoTEnvInfoUnionViewModel = remember {
            SDoTEnvInfoUnionViewModel(coroutineScope)
        }
        LaunchedEffect(key1 = viewModelSDoTEnvInfo){
            while(true){
                delay(5 * 60 * 1000L).let{
                    viewModelSDoTEnvInfo.onEvent(SDoTEnvInfoUnionViewModel.Event.Refresh)
                }
            }
        }

        val caption = "미국 환경보호청(US EPA)의 공식 가이드라인을 바탕으로, 공기질 항목(EPA 기준 오염물질 6종 + 산업/안전 가스 2종)을 6단계로 분류"
        var descriptionBox by remember { mutableStateOf(false) }
        val unHealthyForSensitive =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
        val unHealthy =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
        val veryUnHealthy =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
        val hazardous =  remember{ mutableListOf<Triple<AirQualityManager.AirQualityStage, Float, String>>()}
        val values = remember{ mutableStateOf("" )}
        val maxValue = remember{ mutableStateOf(0.0 )}
        val sDoTEnvInfoStat = remember{ mutableStateOf(emptyList<Pair<AirQualityManager.AirQualityStage, Int>>())}

        val sDoTEnvInfo = viewModelSDoTEnvInfo._sDoTEnvInfoUnionFlow.collectAsState()

        LaunchedEffect( sDoTEnvInfo.value, key2=selectedOption){
            unHealthyForSensitive.clear()
            unHealthy.clear()
            veryUnHealthy.clear()
            hazardous.clear()

            if(sDoTEnvInfo.value.isNotEmpty()) {

                values.value = sDoTEnvInfo.value.map{it}.joinToString(
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

                    if(value > 0f) {
                        val airQualityStage =
                            AirQualityManager.calculateTotalStage(value.toDouble(), selectedOption)

                        when (airQualityStage) {
                            AirQualityManager.AirQualityStage.UNHEALTHY_FOR_SENSITIVE -> {
                                unHealthyForSensitive.add(Triple(airQualityStage, value, it.addr))
                            }

                            AirQualityManager.AirQualityStage.UNHEALTHY -> {
                                unHealthy.add(Triple(airQualityStage, value, it.addr))
                            }
                            AirQualityManager.AirQualityStage.VERY_UNHEALTHY -> {
                                veryUnHealthy.add(Triple(airQualityStage, value, it.addr))
                            }
                            AirQualityManager.AirQualityStage.HAZARDOUS -> {
                                hazardous.add(Triple(airQualityStage, value, it.addr))
                            }

                            else -> {}
                        }
                    }

                    "{ sensing_time:\"${it.sensing_time}\", obs:\"${it.obs}\", lat:${it.lat}, lng:${it.lng},  addr:\"${it.addr}\", value:${value} }"
                }

                maxValue.value  =
                    if (sDoTEnvInfo.value.isEmpty()) 0.0
                    else sDoTEnvInfo.value.map { sensor ->
                        val v = when (selectedOption) {
                            AirQualityManager.ChemicalElement.o3 -> sensor.o3
                            AirQualityManager.ChemicalElement.no2 -> sensor.no2
                            AirQualityManager.ChemicalElement.co -> sensor.co
                            AirQualityManager.ChemicalElement.so2 -> sensor.so2
                            AirQualityManager.ChemicalElement.nh3 -> sensor.nh3
                            AirQualityManager.ChemicalElement.h2s -> sensor.h2s
                            AirQualityManager.ChemicalElement.pm10 -> sensor.pm10
                            AirQualityManager.ChemicalElement.pm25 -> sensor.pm25
                        }
                        v.toDoubleOrNull() ?: 0.0
                    }.max()

                sDoTEnvInfoStat.value = sDoTEnvInfo.value.groupBy { sensor ->

                    val v = when (selectedOption) {
                        AirQualityManager.ChemicalElement.o3 -> sensor.o3
                        AirQualityManager.ChemicalElement.no2 -> sensor.no2
                        AirQualityManager.ChemicalElement.co -> sensor.co
                        AirQualityManager.ChemicalElement.so2 -> sensor.so2
                        AirQualityManager.ChemicalElement.nh3 -> sensor.nh3
                        AirQualityManager.ChemicalElement.h2s -> sensor.h2s
                        AirQualityManager.ChemicalElement.pm10 -> sensor.pm10
                        AirQualityManager.ChemicalElement.pm25 -> sensor.pm25
                    }.toDoubleOrNull() ?: 0.0
                    AirQualityManager.calculateTotalStage(v, selectedOption)
                }.map{ ( airQualityStage, group) ->
                    Pair( airQualityStage ,  group.size)
                }.sortedBy{
                    it.first.level
                }
            }

        }



        val viewModel: KhoaObservationCurrentViewModel = remember {
            KhoaObservationCurrentViewModel(coroutineScope)
        }

        LaunchedEffect(viewModel){
            while(true){
                delay(5 * 60 * 1000L).let{
                    visibleProgressIndicator.value = true
                    viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
                }
            }
        }

        LaunchedEffect(key1 = viewModel){
            viewModel.refreshEvent.collect {
                visibleProgressIndicator.value = false
            }
        }

        val seaWaterInfo = viewModel._observationStateFlow.collectAsState()

        LaunchedEffect( seaWaterInfo.value){
            if (seaWaterInfo.value.isNotEmpty()) {
                sendAddMarkerClusterer(transformToMarkerData(seaWaterInfo.value))
            }
        }

        LaunchedEffect(isReload.value){

            if(isReload.value){
                visibleProgressIndicator.value = true
                viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
                isReload.value = false
                onClickPointOceanWaterInfoGeoChart(initCenterPoint)

            }
        }


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

                            Column(modifier = Modifier.fillMaxSize()) {

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
                                                onClickTabPositionAirInfo(element.name)
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

                                Column (modifier= Modifier.fillMaxWidth()
                                    .weight(1f), // 여기에 weight(1f)를 적용하면 나머지 전체 높이를 차지합니다.
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {

                                    BoxWithConstraints(
                                        modifier = Modifier.fillMaxSize()
                                    ){
                                        val totalWidth = constraints.maxWidth.toFloat()
                                        val height = this.maxHeight

                                        Row(modifier = Modifier.fillMaxSize()) {

                                            var splitFractionVertical by remember { mutableStateOf(0.3f) }

                                            AnimatedVisibility(descriptionBox){

                                                Box(
                                                    modifier = Modifier.fillMaxWidth(
                                                        if(descriptionBox) splitFractionVertical else 0.0f
                                                    ).fillMaxHeight()
                                                        .padding(10.dp)
                                                        .verticalScroll(rememberScrollState())
                                                ) {

                                                    Column(modifier = Modifier.fillMaxSize()) {

                                                        Text(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            text = selectedOption.nameEn(),
                                                            style = MaterialTheme.typography.titleSmall,
                                                            textAlign = TextAlign.Center
                                                        )


                                                        val airQualityStage =
                                                            AirQualityManager.calculateTotalStage(maxValue.value, selectedOption)

                                                        Spacer(Modifier.padding(2.dp))

                                                        AirQualityStatusBoard(airQualityStage, sDoTEnvInfoStat.value)

                                                        caption(caption, Alignment.Center)

                                                        Spacer(Modifier.padding(2.dp))

                                                        AnimatedVisibility(hazardous.isNotEmpty()) {
                                                            val color = AirQualityManager.AirQualityStage.HAZARDOUS.argbColor

                                                            val hazardousText = hazardous.sortedWith(compareByDescending { it.second })
                                                                .mapIndexed { index, triple ->
                                                                    val number = (index + 1).toString().padStart(2, '0') // 01, 02... 형태
                                                                    "$number | ${triple.second} | ${triple.third}"
                                                                }
                                                                .joinToString("\n")

                                                            OutlinedTextField(
                                                                value = hazardousText,
                                                                onValueChange = {}, // 읽기 전용
                                                                readOnly = true,
                                                                textStyle = MaterialTheme.typography.bodySmall,
                                                                label = { Text("위험" )   },
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    unfocusedBorderColor = color,
                                                                    focusedBorderColor = color,
                                                                    focusedLabelColor = color,
                                                                    unfocusedLabelColor = color,
                                                                ),
                                                                modifier = Modifier.fillMaxWidth().height(100.dp)
                                                            )


                                                        }

                                                        Spacer(Modifier.padding(2.dp))

                                                        AnimatedVisibility(veryUnHealthy.isNotEmpty()) {
                                                            val color = AirQualityManager.AirQualityStage.VERY_UNHEALTHY.argbColor

                                                            val veryUnhealthyText = veryUnHealthy.sortedWith(compareByDescending { it.second })
                                                                .mapIndexed { index, triple ->
                                                                    val number = (index + 1).toString().padStart(2, '0') // 01, 02... 형태
                                                                    "$number | ${triple.second} | ${triple.third}"
                                                                }
                                                                .joinToString("\n")

                                                            OutlinedTextField(
                                                                value = veryUnhealthyText,
                                                                onValueChange = {}, // 읽기 전용
                                                                readOnly = true,
                                                                textStyle = MaterialTheme.typography.bodySmall,
                                                                label = { Text("매우 나쁨") },
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    unfocusedBorderColor = color,
                                                                    focusedBorderColor = color,
                                                                    focusedLabelColor = color,
                                                                    unfocusedLabelColor = color,
                                                                ),
                                                                modifier = Modifier.fillMaxWidth().height(100.dp)
                                                            )
                                                        }

                                                        Spacer(Modifier.padding(2.dp))

                                                        AnimatedVisibility(unHealthy.isNotEmpty()) {
                                                            val color = AirQualityManager.AirQualityStage.UNHEALTHY.argbColor

                                                            val unhealthyText = unHealthy.sortedWith(compareByDescending { it.second })
                                                                .mapIndexed { index, triple ->
                                                                    val number = (index + 1).toString().padStart(2, '0') // 01, 02... 형태
                                                                    "$number | ${triple.second} | ${triple.third}"
                                                                }
                                                                .joinToString("\n")

                                                            OutlinedTextField(
                                                                value = unhealthyText,
                                                                onValueChange = {}, // 읽기 전용
                                                                readOnly = true,
                                                                textStyle = MaterialTheme.typography.bodySmall,
                                                                label = { Text("나쁨") },
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    unfocusedBorderColor = color,
                                                                    focusedBorderColor = color,
                                                                    focusedLabelColor = color,
                                                                    unfocusedLabelColor = color,
                                                                ),
                                                                modifier = Modifier.fillMaxWidth().height(100.dp)
                                                            )
                                                        }

                                                        Spacer(Modifier.padding(2.dp))

                                                        AnimatedVisibility(unHealthyForSensitive.isNotEmpty()) {
                                                            val color = AirQualityManager.AirQualityStage.UNHEALTHY_FOR_SENSITIVE.argbColor

                                                            val unHealthyForSensitiveText = unHealthyForSensitive.sortedWith(compareByDescending { it.second })
                                                                .mapIndexed { index, triple ->
                                                                    val number = (index + 1).toString().padStart(2, '0') // 01, 02... 형태
                                                                    "$number | ${triple.second} | ${triple.third}"
                                                                }
                                                                .joinToString("\n")

                                                            OutlinedTextField(
                                                                value = unHealthyForSensitiveText,
                                                                onValueChange = {}, // 읽기 전용
                                                                readOnly = true,
                                                                textStyle = MaterialTheme.typography.bodySmall,
                                                                label = { Text("민감군 영향") },
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    unfocusedBorderColor = color,
                                                                    focusedBorderColor = color,
                                                                    focusedLabelColor = color,
                                                                    unfocusedLabelColor = color,
                                                                ),
                                                                modifier = Modifier.fillMaxWidth().height(100.dp)
                                                            )
                                                        }


                                                        Spacer(Modifier.padding(2.dp))


                                                        BasicTextField(
                                                            value = "Information",
                                                            onValueChange = { },
                                                            readOnly = true,
                                                            decorationBox = { innerTextField ->
                                                                Column(
                                                                    modifier = Modifier.fillMaxWidth()
                                                                        .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                                                                        .padding(10.dp)
                                                                ) {

                                                                    Box( modifier = Modifier.fillMaxWidth(),
                                                                        contentAlignment = Alignment.Center
                                                                    ){
                                                                        innerTextField()
                                                                    }


                                                                    Spacer(Modifier.padding(2.dp))

                                                                    OutlinedTextField(
                                                                        value = selectedOption.specialFeature(),
                                                                        onValueChange = {}, // 읽기 전용
                                                                        readOnly = true,
                                                                        textStyle = MaterialTheme.typography.bodySmall,
                                                                        label = { Text("특징") },
                                                                        shape = RoundedCornerShape(6.dp),
                                                                        modifier = Modifier.fillMaxWidth()
                                                                    )

                                                                    Spacer(Modifier.padding(2.dp))

                                                                    OutlinedTextField(
                                                                        value = selectedOption.unHealthyForSensitiveGroups(),
                                                                        onValueChange = {}, // 읽기 전용
                                                                        readOnly = true,
                                                                        textStyle = MaterialTheme.typography.bodySmall,
                                                                        label = { Text("민감군 영향") },
                                                                        shape = RoundedCornerShape(6.dp),
                                                                        modifier = Modifier.fillMaxWidth()
                                                                    )

                                                                    AirQualityManager.AirQualityStage.entries.forEachIndexed { index, stage ->
                                                                        val range = selectedOption.airQualityStageRange()[index]
                                                                        val comment = selectedOption.airQualityStageComment()[index]

                                                                        if(index > 0 ){
                                                                            OutlinedTextField(
                                                                                value = "${range}\n${comment}",
                                                                                onValueChange = {}, // 읽기 전용
                                                                                readOnly = true,
                                                                                textStyle = MaterialTheme.typography.bodySmall,
                                                                                label = { Text(stage.titleKo) },
                                                                                shape = RoundedCornerShape(6.dp),
                                                                                modifier = Modifier.fillMaxWidth()
                                                                            )
                                                                        }

                                                                    }

                                                                }
                                                            }
                                                        )






                                                    }
                                                }




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
                                                    .height(height)
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





                                    }

                                }

                            }


                        }

                        1 -> {

                            Column(
                                modifier = Modifier.fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .background(color = MaterialTheme.colorScheme.surface),
                                horizontalAlignment = Alignment.CenterHorizontally
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
                                    ) {
                                        WindPolarChart_KHOA()
                                    }

                                    Box(
                                        modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        WaterInfoGeoChart_KHOA(
                                            onClickPointOceanWaterInfoGeoChart
                                        )
                                    }

                                    CompositionLocalProvider(LocalPlatform provides getPlatform()) {




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
                                                val bottomBarOpt = listOf(true, false, false, false)
                                                ChartFeatureControls(
                                                    onChangeFlag = { label, value ->
                                                        when (label) {
                                                            "Reload" -> isReload.value =
                                                                !isReload.value
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
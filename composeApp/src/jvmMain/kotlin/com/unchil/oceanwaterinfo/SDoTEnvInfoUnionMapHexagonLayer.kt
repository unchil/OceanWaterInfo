package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.copy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.AirQualityManager.information
import com.unchil.oceanwaterinfo.AirQualityManager.nameEn
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoUnionViewModel
import kotlinx.coroutines.delay
import javax.swing.tree.DefaultTreeCellEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SDoTEnvInfoUnionMapHexagonLayer(
    initialized: Boolean,
    download:Int,
    errorMessage:String,
){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: SDoTEnvInfoUnionViewModel = remember {
        SDoTEnvInfoUnionViewModel(coroutineScope)
    }
    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(5 * 60 * 1000L).let{
                viewModel.onEvent(SDoTEnvInfoUnionViewModel.Event.Refresh)
            }
        }
    }

    val host = "http://192.168.35.107:7272"
    val servicePage = "sDoTDeckHexagonLayerUnion.html"
    val caption = "미국 환경보호청(US EPA)의 공식 가이드라인을 바탕으로, 공기질 항목(EPA 기준 오염물질 6종 + 산업/안전 가스 2종)을 6단계로 분류"
    var descriptionBox by remember { mutableStateOf(false) }
    val localUrl = "${host}/${servicePage}"
    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()
    val values = remember{ mutableStateOf("" )}
    val maxValue = remember{ mutableStateOf(0.0 )}

    val unHealthyForSensitive =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
    val unHealthy =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
    val veryUnHealthy =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
    val hazardous =  remember{ mutableListOf<Triple<AirQualityManager.AirQualityStage, Float, String>>()}


    val sDoTEnvInfoStat = remember{ mutableStateOf(emptyList<Pair<AirQualityManager.AirQualityStage, Int>>())}

    var selectedOption by remember { mutableStateOf(AirQualityManager.ChemicalElement.entries[0]) }
    val sDoTEnvInfo = viewModel._sDoTEnvInfoUnionFlow.collectAsState()

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

    LaunchedEffect( values.value, webViewState.loadingState, key3 = selectedOption){
        if( values.value.isNotEmpty() &&  webViewState.loadingState is LoadingState.Finished ){
            navigator.evaluateJavaScript("initMapWithData( ${values.value},  \"${selectedOption.name}\")")
        }
    }


    Column(
        modifier = Modifier.fillMaxSize(),
    ) {

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
            contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
        ) {
            AirQualityManager.ChemicalElement.entries.forEachIndexed { index, element ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
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

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            val totalWidth = constraints.maxWidth.toFloat()

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
                                         unfocusedBorderColor = color.copy(0.3f),
                                         focusedBorderColor = color,
                                         focusedLabelColor = color,
                                        unfocusedLabelColor = color.copy(0.3f),
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
                                        unfocusedBorderColor = color.copy(0.3f),
                                        focusedBorderColor = color,
                                        focusedLabelColor = color,
                                        unfocusedLabelColor = color.copy(0.3f),
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
                                        unfocusedBorderColor = color.copy(0.3f),
                                        focusedBorderColor = color,
                                        focusedLabelColor = color,
                                        unfocusedLabelColor = color.copy(0.3f),
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
                                        unfocusedBorderColor = color.copy(0.3f),
                                        focusedBorderColor = color,
                                        focusedLabelColor = color,
                                        unfocusedLabelColor = color.copy(0.3f),
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(100.dp)
                                )
                            }




                            Spacer(Modifier.padding(2.dp))

                            OutlinedTextField(
                                value = selectedOption.information(),
                                onValueChange = {}, // 읽기 전용
                                readOnly = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                                label = { Text("정보") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(600.dp)
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


                when {
                initialized -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CaptionText(
                            AIR_QUAlITY_UNION.caption,
                            textAlign = TextAlign.Center
                        )
                        WebView(
                            state = webViewState,
                            navigator = navigator,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                errorMessage.isNotEmpty() -> {
                    Text(errorMessage)
                }

                else -> {
                    if (download > -1) {
                        Text("Downloading: $download%")
                    } else {
                        Text("Initializing please wait...")
                    }
                    CircularProgressIndicator()

                }
            }

            }

        }

    }
}

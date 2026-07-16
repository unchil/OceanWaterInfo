package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unchil.oceanwaterinfo.AirQualityManager.nameEn
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoUnionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun webMainAirQuality(){

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val bottomBarHeight = remember{100.dp}
    var descriptionBox by remember { mutableStateOf(false) }

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

            sendMsgChangeData.invoke(IFRAME_AIR_INFO, initData.value, selectedOption.name)

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
                                    DIV_WEB_MAIN,
                                    DIV_AIR_INFO
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
                                    sendMsgChangeData.invoke(IFRAME_AIR_INFO, initData.value, selectedOption.name)
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
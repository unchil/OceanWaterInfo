package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhoaWaveInfoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun webMainOceanCurrentWaveMap(){

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val bottomBarHeight = remember{100.dp}

    val initData = remember{ mutableStateOf("" )}
    val visibleProgressIndicator = remember { mutableStateOf(false) }

    val viewModel: KhoaWaveInfoViewModel = remember {
        KhoaWaveInfoViewModel()
    }

    LaunchedEffect(key1 = viewModel){
        while(true){
            viewModel.onEvent(KhoaWaveInfoViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }

    val waveCurrentInfo = viewModel._khoaWaveInfoStateFlow.collectAsState()

    LaunchedEffect(waveCurrentInfo.value){
        if(waveCurrentInfo.value.isNotEmpty()){
            initData.value = waveCurrentInfo.value.map { it }.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ){ it ->
                //Triple(lat,lng,speed)
                "{\"lat\":${it.lat}, \"lng\":${it.lot}, \"height\":${it.wvhgt}, \"period\":${it.wvpd}, \"direction\":${it.wvdrct}, \"name\":\"${it.obsvtrNm}\", \"time\":\"${it.obsrvnDt}\"}"
            }
            sendMsgInitData( IFRAME_SEA_FLOW_HEXAGON, initData.value)
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
                "Ocean Water WaveHeight",
                modifier = Modifier,
            )

            CaptionText(
                "from https://apis.data.go.kr/1192136/noonWave ",
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(height - bottomBarHeight)
                    .onGloballyPositioned { coordinates ->
                        syncHtmlElementPosition(
                            coordinates,
                            density,
                            DIV_WEB_MAIN,
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
                                sendMsgInitData( IFRAME_SEA_FLOW_HEXAGON, initData.value)
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
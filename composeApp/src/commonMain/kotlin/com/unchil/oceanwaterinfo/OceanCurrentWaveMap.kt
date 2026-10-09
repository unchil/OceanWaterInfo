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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhoaWaveInfoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OceanCurrentWaveMap(){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaWaveInfoViewModel = remember {
        KhoaWaveInfoViewModel()
    }

    val host = getPlatform().localServerEndPoint

    val servicePage = "seaFlowMapHexagonLayer.html"

    val localUrl = "${host}/${servicePage}"

    val webController = remember { PlatformWebViewController() }

    LaunchedEffect(key1 = viewModel){
        while(true){
            viewModel.onEvent(KhoaWaveInfoViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }



    val waveCurrentInfo = viewModel._khoaWaveInfoStateFlow.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()


    val values = remember{ mutableStateOf("" )}


    LaunchedEffect(waveCurrentInfo.value){
        if(waveCurrentInfo.value.isNotEmpty()){
            values.value = waveCurrentInfo.value.map { it }.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ){ it ->

                "{\"lat\":${it.lat}, \"lng\":${it.lot}, \"height\":${it.wvhgt}, \"period\":${it.wvpd}, \"direction\":${it.wvdrct}, \"name\":\"${it.obsvtrNm}\", \"time\":\"${it.obsrvnDt}\"}"
            }
        }
    }


    LaunchedEffect(values.value, webController.loadingState){
        if (values.value.isNotEmpty()){
            when(getPlatform().alias){
                PlatformAlias.JVM -> {
                    if ( webController.loadingState?.equals(WebViewLoadingState.Finished) ?: false ) {
                        webController.callJavaScript(
                            functionName = "initMapWithData",
                            args = "${values.value}"
                        )
                    }
                }
                PlatformAlias.IOS, PlatformAlias.ANDROID -> {
                    if ( webController.loadingState?.equals(WebViewLoadingState.Finished) ?: false ) {
                        delay(500)
                        webController.callJavaScript(
                            functionName = "initMapWithData",
                            args = "${values.value}"
                        )
                    }
                }
                else -> {
                    webController.callJavaScript(
                        functionName = "initMapWithData",
                        args = "${values.value}"
                    )
                }
            }

        }

    }



    val bottomBarHeight = remember{80.dp}


    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val height = this.maxHeight

        Column(modifier=Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally)
        {

            Column(
                modifier = Modifier.fillMaxWidth().height(height - bottomBarHeight),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                ChartTitle(
                    "Ocean Water WaveHeight",
                    modifier = Modifier,
                )


                PlatformWebView(
                    url = localUrl,
                    controller = webController,
                    modifier = Modifier.fillMaxSize(),
                )
            }


            CaptionText(
                "from https://apis.data.go.kr/1192136/noonWave ",
                textAlign = TextAlign.Center
            )


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
                                coroutineScope.launch {
                                    viewModel.onEvent(KhoaWaveInfoViewModel.Event.Refresh)
                                }
                                webController.callJavaScript(
                                    functionName = "initMapWithData",
                                    args = "${values.value}"
                                )
                            }
                        }

                    },
                    bottomBarOpt = bottomBarOpt
                )
                if (isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.DarkGray,
                    )
                }
            }

        }

    }
}
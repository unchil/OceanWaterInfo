package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoUnionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirQuality(){
    val coroutineScope = rememberCoroutineScope()

    val viewModel: SDoTEnvInfoUnionViewModel = remember {
        SDoTEnvInfoUnionViewModel()
    }
    LaunchedEffect(key1 = viewModel){
        while(true){
            viewModel.onEvent(SDoTEnvInfoUnionViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }

    val host = getPlatform().localServerEndPoint


    val servicePage = "sDoTDeckHexagonLayerUnion.html"
    var descriptionBox by remember { mutableStateOf(false) }
    val localUrl = "${host}/${servicePage}"


    val webController = remember { PlatformWebViewController() }


    val values = remember{ mutableStateOf("" )}

    var selectedOption by remember { mutableStateOf(AirQualityManager.ChemicalElement.entries[0]) }
    val sDoTEnvInfo = viewModel._sDoTEnvInfoUnionFlow.collectAsState()

    val isLoading = viewModel.isLoading.collectAsState()

    LaunchedEffect( sDoTEnvInfo.value, key2=selectedOption){

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

                "{ sensing_time:\"${it.sensing_time}\", obs:\"${it.obs}\", lat:${it.lat}, lng:${it.lng},  addr:\"${it.addr}\", value:${value} }"
            }
        }
    }


    LaunchedEffect(values.value, webController.loadingState){
        if (values.value.isNotEmpty()){
            when(getPlatform().alias){
                PlatformAlias.JVM -> {
                    if (webController.loadingState is LoadingState.Finished) {
                        webController.callJavaScript(
                            functionName = "initMapWithData",
                            args = "${values.value},  \"${selectedOption.name}\""
                        )
                    }
                }
                PlatformAlias.IOS, PlatformAlias.ANDROID -> {
                    if ( webController.loadingState.toString().equals("Finished")) {
                        delay(500)
                        webController.callJavaScript(
                            functionName = "initMapWithData",
                            args = "${values.value},  \"${selectedOption.name}\""
                        )
                    }
                }
                else -> {
                    webController.callJavaScript(
                        functionName = "initMapWithData",
                        args = "${values.value},  \"${selectedOption.name}\""
                    )
                }
            }

        }

    }


    val bottomBarHeight = remember{80.dp}


    var selectedChemicalElementIndex by remember { mutableIntStateOf(0) }
    val tabClick = {  option: AirQualityManager.ChemicalElement, tabIndex:Int ->
        selectedChemicalElementIndex = tabIndex
        selectedOption = option
    }

    AirQualityMap(
        selectedChemicalElementIndex,
        tabClick
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

                AnimatedVisibility(descriptionBox) {
                    SDoTDescription(
                        sDoTEnvInfo = sDoTEnvInfo.value,
                        selectedOption = selectedOption,
                        splitFractionVertical = splitFractionVertical
                    )
                }

                Box(
                    modifier = Modifier.width(24.dp).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {

                    val rotation by animateFloatAsState(targetValue = if (descriptionBox) 180f else 0f)

                    IconButton(onClick = { descriptionBox = !descriptionBox },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleRight,
                            contentDescription = "Toggle Description",
                            modifier = Modifier.rotate(rotation)
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



                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                        PlatformWebView(
                            url = localUrl,
                            controller = webController,
                            modifier = Modifier.fillMaxSize(),
                        )
                }

            }

            CaptionText(
                AIR_QUAlITY_UNION.caption,
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
                                    viewModel.onEvent(SDoTEnvInfoUnionViewModel.Event.Refresh)
                                }

                                webController.callJavaScript(
                                    functionName = "initMapWithData",
                                    args = "${values.value},  \"${selectedOption.name}\""
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

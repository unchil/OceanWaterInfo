package com.unchil.sdotenvinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.AIR_QUAlITY_UNION
import com.unchil.oceanwaterinfo.AIR_QUAlITY_UNION.desc
import com.unchil.oceanwaterinfo.AIR_QUAlITY_UNION.name
import com.unchil.oceanwaterinfo.CaptionText
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoUnionViewModel
import kotlinx.coroutines.delay


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

    val host = "http://192.168.35.107:7878"
    val servicePage = "sDoTDeckHexagonLayerUnion.html"

    var descriptionBox by remember { mutableStateOf(false) }

    val localUrl = "${host}/${servicePage}"
  //  val localUrl = "http://localhost:63342/OceanWaterInfo/sDoTDeckHexagonLayerUnion.html?_ijt=3edaf4su5b43e54kmglkl4jseh&_ij_reload=RELOAD_ON_SAVE"
    val remoteUrl = "https://www.google.com/maps/"

    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(5 * 60 * 1000L).let{
                viewModel.onEvent(SDoTEnvInfoUnionViewModel.Event.Refresh)
            }
        }
    }


    var selectedOption by remember { mutableStateOf(AIR_QUAlITY_UNION.QualityType.entries[0]) }


    val sDoTEnvInfo = viewModel._sDoTEnvInfoUnionFlow.collectAsState()
    val keys = remember{ mutableStateOf("" )}
    val values = remember{ mutableStateOf("" )}
    val colorRange  = remember{ mutableStateOf("" )}

    LaunchedEffect( sDoTEnvInfo.value, key2=selectedOption){

        if(sDoTEnvInfo.value.isNotEmpty()) {
            values.value = sDoTEnvInfo.value.map{it}.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) { it ->

                val value = when (selectedOption) {
                    AIR_QUAlITY_UNION.QualityType.o3 -> it.o3.ifEmpty { "0" }
                    AIR_QUAlITY_UNION.QualityType.no2 -> it.no2.ifEmpty { "0" }
                    AIR_QUAlITY_UNION.QualityType.co -> it.co.ifEmpty { "0" }
                    AIR_QUAlITY_UNION.QualityType.so2 -> it.so2.ifEmpty { "0" }
                    AIR_QUAlITY_UNION.QualityType.nh3 -> it.nh3.ifEmpty { "0" }
                    AIR_QUAlITY_UNION.QualityType.h2s -> it.h2s.ifEmpty { "0" }
                    AIR_QUAlITY_UNION.QualityType.pm10 -> it.pm10.ifEmpty { "0" }
                    AIR_QUAlITY_UNION.QualityType.pm25 -> it.pm25.ifEmpty { "0" }
                }
                "{ sensing_time:\"${it.sensing_time}\", obs:\"${it.obs}\", lat:${it.lat}, lng:${it.lng},  addr:\"${it.addr}\", value:${value} }"
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
            AIR_QUAlITY_UNION.QualityType.entries.forEachIndexed { index, entrie ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        selectedOption = entrie
                    },
                    text = {
                        Text(
                            text = entrie.name(),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }


        Row(modifier=Modifier.fillMaxSize())
        {
            AnimatedVisibility(descriptionBox){
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.2f)
                        .fillMaxHeight()
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())

                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text =  selectedOption.desc(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Box( modifier = Modifier.width(24.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ){
                val rotation by animateFloatAsState(targetValue = if (descriptionBox) 180f else 0f)

                IconButton(
                    onClick = { descriptionBox = !descriptionBox },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleRight,
                        contentDescription = "Toggle Description",
                        modifier = Modifier.rotate(rotation) // 회전 애니메이션 적용
                    )

                }
            }


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

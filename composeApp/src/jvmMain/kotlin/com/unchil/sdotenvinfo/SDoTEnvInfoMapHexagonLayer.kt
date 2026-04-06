package com.unchil.sdotenvinfo

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.AIR_QUAlITY
import com.unchil.oceanwaterinfo.AIR_QUAlITY.desc
import com.unchil.oceanwaterinfo.AIR_QUAlITY.name
import com.unchil.oceanwaterinfo.CaptionText
import com.unchil.oceanwaterinfo.ChartTitle
import com.unchil.oceanwaterinfo.LocalPoint
import com.unchil.oceanwaterinfo.WATER_QUALITY
import com.unchil.oceanwaterinfo.WATER_QUALITY.name
import com.unchil.oceanwaterinfo.formatLongToDateTime
import com.unchil.oceanwaterinfo.toTidalCurrentDataMap
import com.unchil.oceanwaterinfo.transformToHexagonData
import com.unchil.oceanwaterinfo.viewmodel.KhoaTidalCurrentViewModel
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun SDoTEnvInfoMapHexagonLayer(
    initialized: Boolean,
    download:Int,
    errorMessage:String,
){

    val coroutineScope = rememberCoroutineScope()
    val viewModel: SDoTEnvInfoViewModel = remember {
        SDoTEnvInfoViewModel(coroutineScope)
    }

    val center = LocalPoint.current
    val host = "http://localhost"
    val servicePage = "sDoTDeckHexagonLayer.html"


    var title by remember { mutableStateOf(AIR_QUAlITY.QualityType.entries[0].name()) }

    val localUrl = "${host}/${servicePage}"
    val remoteUrl = "https://www.google.com/maps/"

    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(5 * 60 * 1000L).let{
                viewModel.onEvent(SDoTEnvInfoViewModel.Event.Refresh)
            }
        }
    }
    var hoveredDescription by remember { mutableStateOf<String?>(null) }
    var selectedOption by remember { mutableStateOf(AIR_QUAlITY.QualityType.entries[0]) }


    val sDoTEnvInfo = viewModel._sDoTEnvInfotateFlow.collectAsState()
    val keys = remember{ mutableStateOf("" )}
    val values = remember{ mutableStateOf("" )}


    LaunchedEffect( sDoTEnvInfo.value, key2=selectedOption){


        if(sDoTEnvInfo.value.isNotEmpty()) {
            values.value = sDoTEnvInfo.value.map{it}.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) { it ->

                val maxValue = when (selectedOption) {
                    AIR_QUAlITY.QualityType.max_o3 -> it.max_o3.ifEmpty { "0" }
                    AIR_QUAlITY.QualityType.max_no2 -> it.max_no2.ifEmpty { "0" }
                    AIR_QUAlITY.QualityType.max_co -> it.max_co.ifEmpty { "0" }
                    AIR_QUAlITY.QualityType.max_so2 -> it.max_so2.ifEmpty { "0" }
                    AIR_QUAlITY.QualityType.max_nh3 -> it.max_nh3.ifEmpty { "0" }
                    AIR_QUAlITY.QualityType.max_h2s -> it.max_h2s.ifEmpty { "0" }
                }
                "{ sensing_time:\"${it.sensing_time}\", serial:\"${it.serial}\", lat:${it.lat}, lng:${it.lng},  addr:\"${it.addr}\", value:${maxValue}, title:\"${selectedOption.name()}\" }"
            }
            title = selectedOption.name()
        }
    }

    LaunchedEffect( values.value, webViewState.loadingState, key3 = selectedOption){
        if( values.value.isNotEmpty() &&  webViewState.loadingState is LoadingState.Finished ){
            //       navigator.evaluateJavaScript("alert(\"It's a Beautiful Day.\");" )
            navigator.evaluateJavaScript("initMapWithData( ${values.value})")
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
            AIR_QUAlITY.QualityType.entries.forEachIndexed { index, entrie ->


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


        Text(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            text =  selectedOption.desc(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )

        when {
            initialized -> {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    CaptionText(
                        "from https://data.seoul.go.kr/dataList/OA-15969/S/1/datasetView.do (Seoul Metropolitan Government)",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).padding(end = 20.dp),
                        textAlign = TextAlign.End
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
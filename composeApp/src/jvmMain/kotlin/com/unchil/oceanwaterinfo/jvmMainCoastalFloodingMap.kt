package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.AirQualityManager.nameEn
import com.unchil.oceanwaterinfo.viewmodel.CoastalFloodingInfoViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@Composable
fun jvmMainCoastalFloodingMap(
    initialized: Boolean,
    download:Int,
    errorMessage:String,
){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: CoastalFloodingInfoViewModel = remember {
        CoastalFloodingInfoViewModel()
    }

    val initCenterPoint = remember{ Point(126.934515, 37.385852) }
    val bottomBarHeight = remember{80.dp}


    val host = "http://localhost:7272"
    val servicePage = "coastalFloodingMap.html"

    val localUrl = "${host}/${servicePage}"

    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()

    val geojsonObject = remember{ mutableStateOf(byteArrayOf())}

    var gradeOption by remember { mutableStateOf(CoastalFloodingGrade.entries[0]) }
    var sidoOption by remember { mutableStateOf(SiDo.entries[0]) }



    LaunchedEffect( viewModel, gradeOption, sidoOption){
        viewModel.onEvent(CoastalFloodingInfoViewModel.Event.Refresh(gradeOption.name, sidoOption.name))
    }


    val coastalFloodingInfo = viewModel._coastalFloodingGeoJsonObject.collectAsState()
    // ViewModel의 로딩 상태를 관찰
    val isLoading by viewModel.isLoading.collectAsState()


    LaunchedEffect( coastalFloodingInfo.value){

        if(coastalFloodingInfo.value.isNotEmpty()) {
            geojsonObject.value = coastalFloodingInfo.value.first().geojson

        }
    }

    LaunchedEffect(webViewState.loadingState,geojsonObject.value){
        if( geojsonObject.value.isNotEmpty() &&  webViewState.loadingState is LoadingState.Finished ){
            val jsonString = geojsonObject.value.decodeToString()
            navigator.evaluateJavaScript("initMapWithData( ${jsonString},  \"${gradeOption.name}\")")
        }
    }




    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =   Arrangement.Center
    ) {
        Text(
            "Korea Coastal Flooding Prediction Information",
            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        var selectedTabIndexGrade by remember { mutableIntStateOf(0) }
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndexGrade,
            containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
            contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
        ) {
            CoastalFloodingGrade.entries.forEachIndexed { index, element ->
                Tab(
                    selected = selectedTabIndexGrade == index,
                    onClick = {
                        selectedTabIndexGrade = index
                        gradeOption = element
                    },
                    text = {
                        Text(
                            text = element.tabTitle(),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }


        var selectedTabIndexSido by remember { mutableIntStateOf(0) }
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndexSido,
            containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
            contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
        ) {
            SiDo.entries.forEachIndexed { index, element ->
                Tab(
                    selected = selectedTabIndexSido == index,
                    onClick = {
                        selectedTabIndexSido = index
                        sidoOption = element
                    },
                    text = {
                        Text(
                            text = element.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }




        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val height = this.maxHeight
            when {
                initialized -> {
                    Column(modifier=Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(height - bottomBarHeight),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {


                            WebView(
                                state = webViewState,
                                navigator = navigator,
                                modifier = Modifier.fillMaxSize()
                            )


                        }



                        CaptionText(
                            "from https://apis.data.go.kr/1192136/waterlogged/GetWaterloggedApiService (Korea Hydrographic And Oceanographic Agency)",
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
                                            val jsonString = geojsonObject.value.decodeToString()

                                            navigator.evaluateJavaScript("initMapWithData( ${jsonString},  \"${gradeOption.name}\")")
                                        }
                                    }

                                },
                                bottomBarOpt = bottomBarOpt
                            )

                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.DarkGray,
                                )
                            }

                        }


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
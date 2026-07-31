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
import androidx.compose.material3.Text
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
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.viewmodel.CoastalFloodingInfoViewModel
import kotlinx.coroutines.delay
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

    val host = "http://localhost:7272"
    val servicePage = "coastalFloodingMap.html"

    val localUrl = "${host}/${servicePage}"

 //   val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()

    val initData = remember{ mutableStateOf("" )}

    val grade = "F"

    LaunchedEffect(key1 = viewModel){
        viewModel.onEvent(CoastalFloodingInfoViewModel.Event.Refresh(grade))
    }

    val coastalFloodingInfo = viewModel._coastalFloodingInfo.collectAsState()

    LaunchedEffect( coastalFloodingInfo.value){
        if(coastalFloodingInfo.value.isNotEmpty()) {
            val count = coastalFloodingInfo.value.size


        }
    }

    val bottomBarHeight = remember{60.dp}
    val visibleProgressIndicator = remember { mutableStateOf(false) }


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

                        ChartTitle(
                            "Korea Coastal Flooding Information",
                            modifier = Modifier,
                        )

                        CaptionText(
                            "from https://apis.data.go.kr/1192136/waterlogged/GetWaterloggedApiService (Korea Hydrographic And Oceanographic Agency)",
                            textAlign = TextAlign.Center
                        )

/*
                        WebView(
                            state = webViewState,
                            navigator = navigator,
                            modifier = Modifier.fillMaxSize()
                        )
*/

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
                                        navigator.evaluateJavaScript("initMapWithData( ${initData.value})")
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
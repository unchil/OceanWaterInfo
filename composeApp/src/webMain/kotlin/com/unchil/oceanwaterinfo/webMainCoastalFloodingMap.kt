package com.unchil.oceanwaterinfo


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.CoastalFloodingInfoViewModel
import kotlinx.coroutines.launch


@Composable
fun webMainCoastalFloodingMap(){

    val viewModel: CoastalFloodingInfoViewModel = remember {
        CoastalFloodingInfoViewModel()
    }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val bottomBarHeight = remember{60.dp}
    var gradeOption by remember { mutableStateOf(CoastalFloodingGrade.entries[0]) }
    var sidoOption by remember { mutableStateOf(SiDo.entries[0]) }
    val isVisibleAlert = remember{ mutableStateOf(false)}

    LaunchedEffect( viewModel, gradeOption, sidoOption){
        viewModel.onEvent(CoastalFloodingInfoViewModel.Event.Refresh(gradeOption.name, sidoOption.name))
        println( "[Kotlin] Call Refresh Event")
    }

    val coastalFloodingInfo = viewModel._coastalFloodingGeoJsonObject.collectAsState()
    // ViewModel의 로딩 상태를 관찰
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect( coastalFloodingInfo.value){

        if(coastalFloodingInfo.value.isNotEmpty()) {
            sendPostMsg(IFRAME_COASTAL_FLOODING, "REMOVE_FEATHER" )

            coastalFloodingInfo.value.forEach { it ->
                if(sidoOption.equals(SiDo.entries[0])){
                    println( "[Kotlin] Data Receive (Size: ${it.geojson.length} )")
                    val values = "{ \"grade\": \"${gradeOption.name}\", \"geoJsonData\":${it.geojson}}"
                    sendPostMsg(IFRAME_COASTAL_FLOODING, "COASTAL_FLOODING_ALL", values )
                } else {
                    println( "[Kotlin] Data Receive (Size: ${it.geojson.length})")

                    val values = "{ \"grade\": \"${gradeOption.name}\", \"geoJsonData\":${it.geojson}}"

                    sendPostMsg(IFRAME_COASTAL_FLOODING, "COASTAL_FLOODING", values )
                }
            }
        }
    }

    LaunchedEffect(isLoading) {
        if(!isLoading && coastalFloodingInfo.value.isEmpty()){
            isVisibleAlert.value = true
            sendPostMsg(IFRAME_COASTAL_FLOODING, "EMPTY_DATA" )
        }
    }


    var selectedGradeIndex by remember { mutableIntStateOf(0) }
    var selectedSidoIndex by remember { mutableIntStateOf(0) }
    val tabClick = { tapType:String, grade:CoastalFloodingGrade?, sido:SiDo?, tabIndex:Int ->
        if(tapType.equals("first")){
            selectedGradeIndex = tabIndex
            gradeOption = grade!!
            isVisibleAlert.value = false
        }else{
            selectedSidoIndex = tabIndex
            sidoOption = sido!!
            isVisibleAlert.value = false
        }
    }

    CoastalFloodingMap(selectedGradeIndex, selectedSidoIndex, tabClick){
        val height = this.maxHeight
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            AnimatedVisibility(isVisibleAlert.value) {
                AlertBoxDataNotFound{
                    isVisibleAlert.value = false
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(height - bottomBarHeight)
                    .onGloballyPositioned { coordinates ->
                        syncHtmlElementPosition(
                            coordinates,
                            density,
                            DIV_WEB_MAIN,
                            DIV_COASTAL_FLOODING
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                //
            }

            CaptionText(
                "from https://apis.data.go.kr/1192136/waterlogged/GetWaterloggedApiService (Korea Hydrographic And Oceanographic Agency)",
                textAlign = TextAlign.Center
            )


            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {// [Reload, Tooltips, Symbol, Legend]
                val bottomBarOpt =
                    listOf(true, false, false, false)
                ChartFeatureControls(
                    onChangeFlag = { label, value ->
                        when (label) {
                            "Reload" ->{
                                coroutineScope.launch {
                                    selectedGradeIndex = 0
                                    selectedSidoIndex = 0
                                    gradeOption = CoastalFloodingGrade.entries[0]
                                    sidoOption = SiDo.entries[0]
                                    viewModel.onEvent(CoastalFloodingInfoViewModel.Event.Refresh(gradeOption.name, sidoOption.name))
                                }
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

}
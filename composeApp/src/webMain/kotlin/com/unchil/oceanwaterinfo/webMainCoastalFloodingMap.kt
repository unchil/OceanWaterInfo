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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unchil.oceanwaterinfo.viewmodel.CoastalFloodingInfoViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@Composable
fun webMainCoastalFloodingMap(){

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val bottomBarHeight = remember{100.dp}


    val viewModel: CoastalFloodingInfoViewModel = remember {
        CoastalFloodingInfoViewModel()
    }


    var gradeOption by remember { mutableStateOf(CoastalFloodingGrade.entries[0]) }
    var sidoOption by remember { mutableStateOf(SiDo.entries[0]) }

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
            modifier = Modifier.fillMaxSize()
        ) {
            val height = this.maxHeight
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {


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
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {// [Reload, Tooltips, Symbol, Legend]
                    val bottomBarOpt =
                        listOf(true, false, false, false)
                    ChartFeatureControls(
                        onChangeFlag = { label, value ->
                            when (label) {
                                "Reload" ->{
                                    /*
                                    sendMsgChangeData(
                                        IFRAME_COASTAL_FLOODING,
                                        coastalFloodingInfo.value.first().geojson.decodeToString(),
                                        gradeOption.name
                                    )

                                     */

                                    val values = "{ \"grade\": \"${gradeOption.name}\", \"geoJsonData\":${coastalFloodingInfo.value.first().geojson}}"
                                    sendPostMsg(IFRAME_COASTAL_FLOODING, "COASTAL_FLOODING", values )
                                    coroutineScope.launch {
                                        delay(1000)
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









}
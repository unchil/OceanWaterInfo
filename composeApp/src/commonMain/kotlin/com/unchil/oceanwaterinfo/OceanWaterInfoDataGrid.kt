package com.unchil.oceanwaterinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unchil.oceanwaterinfo.viewmodel.ObservatoryViewModel
import com.unchil.un7datagrid.Un7KCMPDataGrid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val LocalPlatform = compositionLocalOf<Platform> { error("No Platform found!") }


@Composable
fun OceanWaterInfoDataGrid(){

    val coroutineScope = rememberCoroutineScope()

    val isReload = remember { mutableStateOf(false) }
    val visibleProgressIndicator = remember { mutableStateOf(false) }

    val viewModel: NifsSeaWaterInfoCurrentViewModel = remember {
        NifsSeaWaterInfoCurrentViewModel(  coroutineScope  )
    }

    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(5 * 60 * 1000L).let{
                viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var isVisible by remember { mutableStateOf(false) }



    LaunchedEffect(isReload.value){
        if(isReload.value){
            viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
            visibleProgressIndicator.value = true
            delay(1000)
            visibleProgressIndicator.value = false
            isReload.value = false
        }
    }

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()

    val gridData = remember { mutableStateOf(mapOf<String, List<Any?>>() ) }

    LaunchedEffect(seaWaterInfo.value){
        isVisible = seaWaterInfo.value.isNotEmpty()
        if(isVisible){
            gridData.value = seaWaterInfo.value.toGridDataMap()
        }
    }

    MaterialTheme{
        Box{
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isVisible) {

                    Text(
                        "${seaWaterInfo.value.first().obs_datetime} Ocean Water Temperature Data",
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                    )


                    Un7KCMPDataGrid(
                        gridData.value,
                        modifier = Modifier
                            .height(350.dp)
                            .border(border = BorderStroke(width=1.dp, Color.LightGray), shape = ShapeDefaults.ExtraSmall),
                        onClick = { rowsData ->
                            /*
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Selected Rows : ${rowsData}"
                                )
                            }

                             */
                        },
                        onLongClick = { rowsData ->
                            /*
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Selected Rows : ${rowsData}"
                                )
                            }

                             */
                        },
                    )

                    Text(
                        "from https://www.nifs.go.kr (National Institute of Fisheries Science)",
                        modifier = Modifier.fillMaxWidth().padding(end = 40.dp),
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Right
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ){// [Reload, Tooltips, Symbol, Legend]
                        val bottomBarOpt = listOf(true, false, false, false)
                        ChartFeatureControls(
                            onChangeFlag = { label, value ->
                                when(label){
                                    "Reload" ->  isReload.value = !isReload.value
                                }
                            },
                            bottomBarOpt = bottomBarOpt
                        )
                        if(visibleProgressIndicator.value){
                            CircularProgressIndicator(
                                color = Color.DarkGray,
                            )
                        }
                    }
                }

            }//--- Column

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

        }

    }
}
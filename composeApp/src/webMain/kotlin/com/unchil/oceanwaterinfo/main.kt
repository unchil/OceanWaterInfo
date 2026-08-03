package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import com.unchil.oceanwaterinfo.AirQualityManager.nameEn
import com.unchil.oceanwaterinfo.viewmodel.KhoaTidalCurrentViewModel
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoUnionViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main(){

    ComposeViewport(viewportContainerId = DIV_WEB_MAIN) {

        var selectedTabIndex by remember { mutableStateOf(0) } // 탭 인덱스 상태

        LaunchedEffect(selectedTabIndex) {
            changeSelectedTab(selectedTabIndex)
        }

        MaterialTheme(
            typography = getTypography(),
            colorScheme = getColorScheme(false))
        {

            Column(
                modifier = Modifier.fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {

                SecondaryTabRow(
                    selectedTabIndex,
                    Modifier.fillMaxWidth(),
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.primary,
                    { HorizontalDivider() }
                ) {
                    MAIN_TAB_ITEMS.forEachIndexed { index, title ->

                        val interactionSource = remember { MutableInteractionSource() }
                        // InteractionSource의 상태 변화를 직접 감지하는 로직
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collectLatest { interaction ->
                                when (interaction) {
                                    is PressInteraction.Press -> {
                                        if (selectedTabIndex != index) {
                                            selectedTabIndex = index
                                        }
                                    }
                                }
                            }
                        }

                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                // 고수준 onClick도 유지하되, 위 LaunchedEffect가 보조 역할을 수행합니다.
                                if (selectedTabIndex != index) {
                                    selectedTabIndex = index
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Light,
                                    // 리사이즈 도중 텍스트가 잘려나가는 것을 방지
                                    softWrap = false,
                                    maxLines = 1
                                )
                            },
                            // interactionSource를 명시적으로 관리하면 시스템 부하 상황에서 더 잘 반응함
                            interactionSource = interactionSource
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTabIndex) {
                        0 -> {
                            webMainAirQuality()
                        }
                        1 -> {
                            webMainOceanWaterQuality()
                        }
                        2-> {
                            webMainTidalForecastMap()
                        }

                        3 -> {
                            webMainOceanCurrentSpeedMap()
                        }

                        4 -> {
                            webMainHydroNuclearPower()
                        }
                        5 -> {
                            webMainCoastalFloodingMap()
                        }
                    }
                }

            }


        } // MaterialTheme


        DisposableEffect(Unit) {
            onDispose {
                disposeHtmlElements(listOf(DIV_AIR_INFO, DIV_OCEAN_WATER_INFO, DIV_SEA_FLOW_TRIPS, DIV_SEA_FLOW_HEXAGON, DIV_WATER_INFO))
            }
        }

    }
}
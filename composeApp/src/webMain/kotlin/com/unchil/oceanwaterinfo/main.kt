package com.unchil.oceanwaterinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        CompositionLocalProvider(LocalPlatform provides getPlatform()) {

            MaterialTheme(
                typography = getTypography(),
                colorScheme = getColorScheme(false)
            ) {

                var splitFractionHorizontal by remember { mutableStateOf(0.5f) }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val totalHeight = constraints.maxHeight.toFloat()


                    Column(modifier = Modifier.fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.surface)
                    ) {

                        Text(
                            "Korea Ocean Water Information",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Box(modifier = Modifier.fillMaxHeight(splitFractionHorizontal)) {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                OceanWaterInfoBoxPlotChart()

                                OceanWaterInfoBarChart()

                                OceanWaterInfoLineChart()

                                OceanWaterInfoLineChart_MOF()
                            }

                        }

                        DraggableHorizontalDivider(
                            onDrag = { deltaPx ->
                                val deltaWeight = deltaPx / totalHeight
                                // 박스 2의 크기를 조절
                                splitFractionHorizontal = (splitFractionHorizontal + deltaWeight).coerceIn(0.1f, 0.9f)
                            }
                        )

                        Box(
                            modifier=paddingMod,
                            contentAlignment = Alignment.Center
                        ){

                            OceanWaterInfoDataGrid()
                        }

                    }


                }

            }

        }
    }
}
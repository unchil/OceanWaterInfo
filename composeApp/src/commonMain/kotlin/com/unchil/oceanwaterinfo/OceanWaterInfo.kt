package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
@UiComposable
fun OceanWaterInfo(){



            Column(
                modifier = paddingMod.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .safeContentPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    "Korea Ocean Water Information",
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )

                OceanWaterInfoBoxPlotChart()

                OceanWaterInfoLineChart()

                OceanWaterInfoBarChart()

                OceanWaterInfoLineChart_MOF()

                OceanWaterInfoDataGrid()

                OceanWaterInfoGeoChart()

            }


}
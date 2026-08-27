package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


val HydroNuclearPower = object : SimpleChartView {
    override val name: String = "HydroNuclearPower"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        HydroNuclearPower()
    }
}

@Composable
fun HydroNuclearPower(){
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NuclearPlantStatePieChart_KHNP()
        RadioActiveWastePlantStatStackedBarChart_KHNP()
        KHNPRadioActiveWasteStackBarChart()
        WaterTempTimeSeries_KHOA()
        RadioRateBarChart()
        WasteWaterTimeSeries_KHNP()
        ThermalWasteWaterTimeSeries_KHNP()
        WaterDegTimeSeries_KHOA()
    }
}

val OceanWaterQuality = object : SimpleChartView {
    override val name: String = "OceanWaterQuality"

    override fun toString(): String = name

    @Composable
    override fun Content() {
        OceanWaterQuality()
    }
}


@Composable
fun OceanWaterQuality(){
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        OceanWaterInfoTimeSeries()
        OceanWaterInfoBoxPlotChart()
        OceanWaterInfoBarChart()

    }
}

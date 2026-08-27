package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


val NuclearPlantStatePieChart_KHNP = object : SimpleChartView {
    override val name: String = "NuclearPlantStatePieChart_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NuclearPlantStatePieChart_KHNP()
        }
    }
}


val RadioActiveWastePlantStatStackedBarChart_KHNP = object : SimpleChartView {
    override val name: String = "RadioActiveWastePlantStatStackedBarChart_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RadioActiveWastePlantStatStackedBarChart_KHNP()
        }
    }
}

val KHNPRadioActiveWasteStackBarChart = object : SimpleChartView {
    override val name: String = "KHNPRadioActiveWasteStackBarChart"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KHNPRadioActiveWasteStackBarChart()
        }
    }
}

val WaterTempTimeSeries_KHOA = object : SimpleChartView {
    override val name: String = "WaterTempTimeSeries_KHOA"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            WaterTempTimeSeries_KHOA()
        }
    }
}

val RadioRateBarChart = object : SimpleChartView {
    override val name: String = "RadioRateBarChart"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RadioRateBarChart()
        }
    }
}

val WasteWaterTimeSeries_KHNP = object : SimpleChartView {
    override val name: String = "WasteWaterTimeSeries_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            WasteWaterTimeSeries_KHNP()
        }
    }
}

val ThermalWasteWaterTimeSeries_KHNP = object : SimpleChartView {
    override val name: String = "ThermalWasteWaterTimeSeries_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            ThermalWasteWaterTimeSeries_KHNP()
        }
    }
}


val WaterDegTimeSeries_KHOA = object : SimpleChartView {
    override val name: String = "WaterDegTimeSeries_KHOA"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            WaterDegTimeSeries_KHOA()
        }
    }
}


val OceanWaterInfoTimeSeries = object : SimpleChartView {
    override val name: String = "OceanWaterInfoTimeSeries"

    override fun toString(): String = name

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            OceanWaterInfoTimeSeries()
        }
    }
}

val OceanWaterInfoBoxPlotChart = object : SimpleChartView {
    override val name: String = "OceanWaterInfoBoxPlotChart"

    override fun toString(): String = name

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            OceanWaterInfoBoxPlotChart()
        }
    }
}


val OceanWaterInfoBarChart = object : SimpleChartView {
    override val name: String = "OceanWaterInfoBarChart"

    override fun toString(): String = name

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            OceanWaterInfoBarChart()
        }
    }
}



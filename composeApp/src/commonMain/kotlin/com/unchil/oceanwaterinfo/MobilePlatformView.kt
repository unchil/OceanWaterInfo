package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val ChartBottonBarHeight = 120.dp

val OceanWaterInfoDataGrid = object : SimpleChartView {
    override val name: String = "OceanWaterInfoDataGrid"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OceanWaterInfoDataGrid()
        }
    }
}

val WindPolarChart_KHNP = object : SimpleChartView {
    override val name: String = "WindPolarChart_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            WindPolarChart_KHOA(this.maxHeight - ChartBottonBarHeight)
        }

    }
}
val NuclearPlantStatePieChart_KHNP = object : SimpleChartView {
    override val name: String = "NuclearPlantStatePieChart_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {


        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            NuclearPlantStatePieChart_KHNP(this.maxHeight - ChartBottonBarHeight)
        }
    }
}


val RadioActiveWastePlantStatStackedBarChart_KHNP = object : SimpleChartView {
    override val name: String = "RadioActiveWastePlantStatStackedBarChart_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            RadioActiveWastePlantStatStackedBarChart_KHNP(this.maxHeight - ChartBottonBarHeight)
        }

    }
}

val KHNPRadioActiveWasteStackBarChart = object : SimpleChartView {
    override val name: String = "KHNPRadioActiveWasteStackBarChart"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            KHNPRadioActiveWasteStackBarChart(this.maxHeight - ChartBottonBarHeight)
        }

    }
}

val WaterTempTimeSeries_KHOA = object : SimpleChartView {
    override val name: String = "WaterTempTimeSeries_KHOA"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            WaterTempTimeSeries_KHOA(this.maxHeight - ChartBottonBarHeight)
        }

    }
}

val RadioRateBarChart = object : SimpleChartView {
    override val name: String = "RadioRateBarChart"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            RadioRateBarChart(this.maxHeight - ChartBottonBarHeight)
        }
    }
}

val WasteWaterTimeSeries_KHNP = object : SimpleChartView {
    override val name: String = "WasteWaterTimeSeries_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            WasteWaterTimeSeries_KHNP(this.maxHeight - ChartBottonBarHeight)
        }
    }
}

val ThermalWasteWaterTimeSeries_KHNP = object : SimpleChartView {
    override val name: String = "ThermalWasteWaterTimeSeries_KHNP"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ThermalWasteWaterTimeSeries_KHNP(this.maxHeight - ChartBottonBarHeight)
        }
    }
}


val WaterDegTimeSeries_KHOA = object : SimpleChartView {
    override val name: String = "WaterDegTimeSeries_KHOA"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            WaterDegTimeSeries_KHOA(this.maxHeight - ChartBottonBarHeight)
        }
    }
}


val OceanWaterInfoTimeSeries = object : SimpleChartView {
    override val name: String = "OceanWaterInfoTimeSeries"

    override fun toString(): String = name

    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            OceanWaterInfoTimeSeries(this.maxHeight - ChartBottonBarHeight)
        }
    }
}

val OceanWaterInfoBoxPlotChart = object : SimpleChartView {
    override val name: String = "OceanWaterInfoBoxPlotChart"

    override fun toString(): String = name

    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            OceanWaterInfoBoxPlotChart(this.maxHeight - ChartBottonBarHeight)
        }
    }
}


val OceanWaterInfoBarChart = object : SimpleChartView {
    override val name: String = "OceanWaterInfoBarChart"

    override fun toString(): String = name

    @Composable
    override fun Content() {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            OceanWaterInfoBarChart(this.maxHeight - ChartBottonBarHeight)
        }
    }
}


val OceanWaterInfoGeoChart = object : SimpleChartView {
    override val name: String = "OceanWaterInfoGeoChart"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            OceanWaterInfoGeoChart(this.maxHeight - ChartBottonBarHeight)
        }
    }
}

val OceanWaterInfo_MOF = object : SimpleChartView {
    override val name: String = "OceanWaterInfo_MOF"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            OceanWaterInfo_MOF(this.maxHeight - ChartBottonBarHeight)
        }
    }
}





val TestWebViewScreen = object : SimpleChartView {
    override val name: String = "TestWebViewScreen"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize(),
            //    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TestWebViewScreen()
        }
    }
}

val AirQuality= object : SimpleChartView {
    override val name: String = "AirQuality"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AirQuality()
        }
    }
}

val TidalForecastMap= object : SimpleChartView {
    override val name: String = "TidalForecastMap"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TidalForecastMap()
        }
    }
}

val OceanCurrentSpeedMap = object : SimpleChartView {
    override val name: String = "OceanCurrentSpeedMap"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OceanCurrentSpeedMap()
        }
    }
}

val CoastalFloodingMap = object : SimpleChartView {
    override val name: String = "CoastalFloodingMap"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CoastalFloodingMap()
        }
    }
}

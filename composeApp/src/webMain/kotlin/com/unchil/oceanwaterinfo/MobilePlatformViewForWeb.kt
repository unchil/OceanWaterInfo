package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unchil.oceanwaterinfo.util.MainTab
import com.unchil.oceanwaterinfo.util.MainTab.elementID
import com.unchil.oceanwaterinfo.util.MainTab.iframeID
import io.github.vooft.compose.treeview.core.node.Branch
import io.github.vooft.compose.treeview.core.node.Leaf
import io.github.vooft.compose.treeview.core.tree.Tree


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun envObservationTreeForWeb(): Tree<ChartViewContent> = Tree {

    Branch(ChartViewCategory("Ocean Water Quality")) {
        Leaf<ChartViewContent>(WaterDegTimeSeries_KHOA)
        Leaf<ChartViewContent>(OceanWaterInfoTimeSeries)
        Leaf<ChartViewContent>(OceanWaterInfoBoxPlotChart)
        Leaf<ChartViewContent>(OceanWaterInfoBarChart)
        Leaf<ChartViewContent>(OceanWaterInfoGeoChart)
        Leaf<ChartViewContent>(OceanWaterInfo_MOF)

    }

    Branch(ChartViewCategory("Hydro Nuclear Power")) {
        Leaf<ChartViewContent>(WindPolarChart_KHNP)
        Leaf<ChartViewContent>(NuclearPlantStatePieChart_KHNP)
        Leaf<ChartViewContent>(RadioActiveWastePlantStatStackedBarChart_KHNP)
        Leaf<ChartViewContent>(KHNPRadioActiveWasteStackBarChart)
        Leaf<ChartViewContent>(WaterTempTimeSeries_KHOA)
        Leaf<ChartViewContent>(RadioRateBarChart)
        Leaf<ChartViewContent>(WasteWaterTimeSeries_KHNP)
        Leaf<ChartViewContent>(ThermalWasteWaterTimeSeries_KHNP)
    }

    /*
    Branch(ChartViewCategory("Prediction Information Map")){
        Leaf<ChartViewContent>(AirQualityForWeb)
        Leaf<ChartViewContent>(CoastalFloodingMapForWeb)
        Leaf<ChartViewContent>(TidalForecastMapForWeb)
        Leaf<ChartViewContent>(OceanCurrentWaveMapForWeb)
    }

     */


    Leaf<ChartViewContent>(OceanWaterInfoDataGrid)

}


val AirQualityForWeb= object : SimpleChartView {
    override val name: String = "AirQualityForWeb"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        // 컴포넌트가 화면에 그려질 때 가시성 표시 및 iframe 새로고침 실행
        LaunchedEffect(Unit) {
            showAndReloadIframe(
                elementId = MainTab.TabItem.AIR_QUALITY.elementID(),
                iframeId = MainTab.TabItem.AIR_QUALITY.iframeID()
            )
        }


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            webMainAirQuality()
        }
    }
}

val TidalForecastMapForWeb= object : SimpleChartView {
    override val name: String = "TidalForecastMapForWeb"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        LaunchedEffect(Unit) {
            showAndReloadIframe(
                elementId = MainTab.TabItem.OCEAN_TIDAL_SPEED.elementID(),
                iframeId = MainTab.TabItem.OCEAN_TIDAL_SPEED.iframeID()
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            webMainTidalForecastMap()
        }
    }
}

val OceanCurrentWaveMapForWeb = object : SimpleChartView {
    override val name: String = "OceanCurrentWaveMapForWeb"
    override fun toString(): String = name
    @Composable
    override fun Content() {
        LaunchedEffect(Unit) {
            showAndReloadIframe(
                elementId = MainTab.TabItem.OCEAN_WAVE_HEIGHT.elementID(),
                iframeId = MainTab.TabItem.OCEAN_WAVE_HEIGHT.iframeID()
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            webMainOceanCurrentWaveMap()
        }
    }
}

val CoastalFloodingMapForWeb = object : SimpleChartView {
    override val name: String = "CoastalFloodingMapForWeb"
    override fun toString(): String = name
    @Composable
    override fun Content() {

        LaunchedEffect(Unit) {
            showAndReloadIframe(
                elementId = MainTab.TabItem.COASTAL_FLOODING.elementID(),
                iframeId = MainTab.TabItem.COASTAL_FLOODING.iframeID()
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            webMainCoastalFloodingMap()
        }
    }
}

package com.unchil.oceanwaterinfo

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
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


    Leaf<ChartViewContent>(OceanWaterInfoDataGrid)

}

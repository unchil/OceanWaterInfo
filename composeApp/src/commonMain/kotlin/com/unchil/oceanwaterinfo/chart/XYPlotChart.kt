package com.unchil.oceanwaterinfo.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.AxisLabel
import com.unchil.oceanwaterinfo.BoxPlot
import com.unchil.oceanwaterinfo.ChartData
import com.unchil.oceanwaterinfo.ChartTitle
import com.unchil.oceanwaterinfo.ChartType
import com.unchil.oceanwaterinfo.GeoChart
import com.unchil.oceanwaterinfo.LayoutData
import com.unchil.oceanwaterinfo.Legend
import com.unchil.oceanwaterinfo.LineChart
import com.unchil.oceanwaterinfo.VerticalBarChart
import com.unchil.oceanwaterinfo.caption
import com.unchil.oceanwaterinfo.description
import com.unchil.oceanwaterinfo.formatLongToDateTime
import com.unchil.oceanwaterinfo.getColors
import com.unchil.oceanwaterinfo.paddingMod
import com.unchil.oceanwaterinfo.xTitle
import com.unchil.oceanwaterinfo.yTitle
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.AxisContent
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberAxisStyle
import io.github.koalaplot.core.xygraph.rememberGridStyle
import kotlin.math.round

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun XYPlotChart(
    layout: LayoutData,
    chartData: ChartData,
    entries:List<String>
){


    val xStyle = if (layout.xAxis.style == null){
        rememberAxisStyle()
    }else{
        AxisStyle(
            color = layout.xAxis.style.color ,
            majorTickSize = layout.xAxis.style.majorTickSize,
            minorTickSize = layout.xAxis.style.minorTickSize,
            tickPosition = layout.xAxis.style.tickPosition,
            lineWidth = layout.xAxis.style.lineWidth,
            labelRotation = layout.xAxis.style.labelRotation
        )
    }

    val yStyle = if (layout.yAxis.style == null){
        rememberAxisStyle()
    }else{
        AxisStyle(
            color = layout.yAxis.style.color,
            majorTickSize = layout.yAxis.style.majorTickSize,
            minorTickSize = layout.yAxis.style.minorTickSize,
            tickPosition = layout.yAxis.style.tickPosition,
            lineWidth = layout.yAxis.style.lineWidth,
            labelRotation = layout.yAxis.style.labelRotation
        )
    }

    val gridStyle = if(layout.gridStyle == null){
        rememberGridStyle()
    }else {
        rememberGridStyle(
            horizontalMajorStyle = layout.gridStyle.horizontalMajorStyle ,
            horizontalMinorStyle = layout.gridStyle.horizontalMinorStyle ,
            verticalMajorStyle = layout.gridStyle.verticalMajorStyle,
            verticalMinorStyle = layout.gridStyle.verticalMinorStyle
        )
    }

    val modifier = when(layout.type){
        ChartType.Geo -> {
            Modifier
                .width(layout.size.height*1.2f)
                .height(layout.size.height)
        }
        else -> {
            Modifier
                .fillMaxWidth()
                .height(layout.size.height)
        }
    }


    Box( modifier = modifier,
        contentAlignment =  Alignment.Center
    ) {

        ChartLayout(
            modifier = paddingMod
                .sizeIn(minHeight = layout.size.minHeight, maxHeight = layout.size.maxHeight)
                .background(color = MaterialTheme.colorScheme.surface),
            title = { if (layout.layout.isTitle) { ChartTitle(layout.layout.title, modifier = paddingMod)  } },
            legend = { if(layout.legend.isUsable ) { Legend(layout, entries) }  },
            legendLocation = layout.legend.location
        ) {

            Column {

                if (!layout.layout.description.isNullOrBlank()) description

                when(chartData){

                    is ChartData.TimeSeries -> {
                        XYGraph (
                            xAxisModel = layout.xAxis.model as DoubleLinearAxisModel,
                            yAxisModel = layout.yAxis.model as FloatLinearAxisModel,
                            xAxisContent = AxisContent(
                                labels = {
                                    if (layout.xAxis.isLabels) { AxisLabel(formatLongToDateTime(it), Modifier.padding(top = 2.dp)) }
                                },
                                title = { if (layout.xAxis.isTitle)  xTitle(layout.xAxis.title) },
                                style = xStyle,
                            ),
                            yAxisContent = AxisContent(
                                labels = {
                                    if (layout.yAxis.isLabels) { AxisLabel((round(it * 10) / 10f).toString(), Modifier.absolutePadding(right = 2.dp)) }
                                },
                                title = {if (layout.yAxis.isTitle) yTitle(layout.yAxis.title) },
                                style = yStyle
                            ),
                            gridStyle  = gridStyle,
                            modifier = Modifier.padding(horizontal = 2.dp)

                        ){
                            when (layout.type) {
                                ChartType.Line ->
                                    LineChart(chartData, layout.tooltips.isTooltips, layout.tooltips.isSymbol)
                                ChartType.DegLine ->
                                    DegLineChart(chartData, layout.tooltips.isTooltips, layout.tooltips.isSymbol)
                                ChartType.Point ->
                                    PointLineChart(chartData, layout.tooltips.isTooltips, layout.tooltips.isSymbol)
                                ChartType.Area ->
                                    AreaLineChart(chartData, layout.tooltips.isTooltips, layout.tooltips.isSymbol)
                                else -> {}
                            }
                        }
                    }
                    is ChartData.XYPlotStringFloat  -> {
                        XYGraph (
                            xAxisModel = layout.xAxis.model as CategoryAxisModel<String>,
                            yAxisModel = layout.yAxis.model as FloatLinearAxisModel,
                            xAxisContent = AxisContent(
                                labels = {
                                    if (layout.xAxis.isLabels) { AxisLabel(it, Modifier.padding(top = 2.dp))}
                                },
                                title = {  if (layout.xAxis.isTitle)  xTitle(layout.xAxis.title) },
                                style = xStyle,
                            ),
                            yAxisContent = AxisContent(
                                labels = {
                                    if (layout.yAxis.isLabels) { AxisLabel((round(it * 10) / 10f).toString(), Modifier.absolutePadding(right = 2.dp)) }
                                },
                                title = { if (layout.yAxis.isTitle)  yTitle(layout.yAxis.title )} ,
                                style = yStyle
                            ),
                            gridStyle  = gridStyle,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ){
                            when (layout.type) {
                                ChartType.VerticalBar -> {
                                    VerticalBarChart (
                                        chartData,
                                        layout.tooltips.isTooltips,
                                        layout.barConf.widthWeight
                                    )
                                }
                                else -> {}
                            }
                        }

                    }
                    is ChartData.XYPlotBoxPlot -> {
                        XYGraph (
                            xAxisModel = layout.xAxis.model as CategoryAxisModel<String>,
                            yAxisModel = layout.yAxis.model as FloatLinearAxisModel,
                            xAxisContent = AxisContent(
                                labels = {
                                    if (layout.xAxis.isLabels) { AxisLabel(it, Modifier.padding(top = 2.dp))}
                                },
                                title = {  if (layout.xAxis.isTitle)  xTitle(layout.xAxis.title) },
                                style = xStyle,
                            ),
                            yAxisContent = AxisContent(
                                labels = {
                                    if (layout.yAxis.isLabels) { AxisLabel((round(it * 10) / 10f).toString(), Modifier.absolutePadding(right = 2.dp)) }
                                },
                                title = { if (layout.yAxis.isTitle)  yTitle(layout.yAxis.title )} ,
                                style = yStyle
                            ),
                            gridStyle  = gridStyle,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ){
                            when (layout.type) {
                                ChartType.BoxPlot -> {
                                    BoxPlot(chartData, layout.tooltips.isTooltips )
                                }
                                else -> {}
                            }
                        }


                    }
                    is ChartData.XYPlotGeoPlot -> {
                        XYGraph (
                            xAxisModel = layout.xAxis.model as DoubleLinearAxisModel,
                            yAxisModel = layout.yAxis.model as DoubleLinearAxisModel,
                            xAxisContent = AxisContent(
                                labels = {
                                    if (layout.xAxis.isLabels) {  AxisLabel(it.toString(), Modifier.padding(top = 2.dp))}
                                },
                                title = {  if (layout.xAxis.isTitle)  xTitle(layout.xAxis.title) },
                                style = xStyle,
                            ),
                            yAxisContent = AxisContent(
                                labels = {
                                    if (layout.yAxis.isLabels) {
                                          AxisLabel((round(it * 10) / 10.0).toString(), Modifier.absolutePadding(right = 2.dp))
                                    }
                                },
                                title = { if (layout.yAxis.isTitle)  yTitle(layout.yAxis.title )} ,
                                style = yStyle
                            ),
                            gridStyle  = gridStyle,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ){
                            when (layout.type) {
                                ChartType.Geo -> {
                                    GeoChart(chartData, layout.tooltips.isTooltips )
                                }
                                else -> {}
                            }
                        }

                    }

                    else -> {  }
                }// when(ChartData)

            } // Column
        } // ChartLayout


        if (layout.caption.isCaption) caption(layout.caption.title, layout.caption.location) //-- Caption

    } // Box


}
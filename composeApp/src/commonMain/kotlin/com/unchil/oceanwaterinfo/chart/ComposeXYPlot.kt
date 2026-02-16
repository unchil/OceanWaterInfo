package com.unchil.oceanwaterinfo


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.AxisContent
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.XYGraphScope
import io.github.koalaplot.core.xygraph.rememberGridStyle

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun ComposeXYPlot(
    layout: LayoutData,
    data:Any,
    entries:List<String>
){
    val colors = getColors(entries)

    val modifier = when(layout.type){
        ChartType.XYGraph, ChartType.Line, ChartType.VerticalBar, ChartType.GroupVerticalBar, ChartType.BoxPlot -> {
            Modifier
                .fillMaxWidth()
                .height(layout.size.height)
        }
        ChartType.Geo -> {
            Modifier
                .width(layout.size.height*1.2f)
                .height(layout.size.height)
        }
    }

    Box( modifier =modifier,
        contentAlignment =  Alignment.Center
    ) {
        ChartLayout(
            modifier = paddingMod
                .sizeIn(minHeight = layout.size.minHeight, maxHeight = layout.size.maxHeight)
                .background(color = MaterialTheme.colorScheme.surface),
            title = {
                if (layout.layout.isTitle) {
                    ChartTitle(layout.layout.title, modifier = paddingMod)
                }
            },
            legend = {
                if(layout.legend.isUsable ) {
                    Legend(layout, entries, colors)
                }
            },
            legendLocation = layout.legend.location
        ) {
            Column {

                if(!layout.layout.description.isNullOrBlank()){
                    Text(
                        text = layout.layout.description,
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        textAlign = TextAlign.Start
                    )
                    HorizontalDivider(modifier = Modifier.padding(10.dp))
                }

                XYGraph(
                    xAxisModel = when (layout.type) {
                        ChartType.Line, ChartType.Geo -> layout.xAxis.model as DoubleLinearAxisModel
                        ChartType.VerticalBar, ChartType.BoxPlot, ChartType.GroupVerticalBar, ChartType.XYGraph -> {
                            layout.xAxis.model as CategoryAxisModel<Any>
                        }
                    },
                    yAxisModel = when (layout.type) {
                        ChartType.XYGraph, ChartType.VerticalBar, ChartType.BoxPlot,ChartType.GroupVerticalBar, ChartType.Line -> {
                            layout.yAxis.model as FloatLinearAxisModel
                        }
                        ChartType.Geo -> layout.yAxis.model as DoubleLinearAxisModel
                    },
                    xAxisContent = AxisContent(
                        labels = {
                            if (layout.xAxis.isLabels) {
                                when (layout.type) {
                                    ChartType.XYGraph, ChartType.VerticalBar, ChartType.GroupVerticalBar, ChartType.BoxPlot -> {
                                        AxisLabel(it.toString(), Modifier.padding(top = 2.dp))
                                    }
                                    ChartType.Line -> {
                                        AxisLabel(formatLongToDateTime(it), Modifier.padding(top = 2.dp))
                                    }
                                    ChartType.Geo ->{
                                        AxisLabel(it.toString(), Modifier.padding(top = 2.dp))
                                    }
                                }
                            }
                        },
                        title = {
                            if (layout.xAxis.isTitle) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AxisTitle(layout.xAxis.title, paddingMod)
                                }
                            }
                        },
                        style = AxisStyle(
                            color = layout.xAxis.style?.color ?: KoalaPlotTheme.axis.color,
                            majorTickSize = layout.xAxis.style?.majorTickSize
                                ?: KoalaPlotTheme.axis.majorTickSize,
                            minorTickSize = layout.xAxis.style?.minorTickSize
                                ?: KoalaPlotTheme.axis.minorTickSize,
                            tickPosition = layout.xAxis.style?.tickPosition
                                ?: KoalaPlotTheme.axis.xyGraphTickPosition,
                            lineWidth = layout.xAxis.style?.lineWidth
                                ?: KoalaPlotTheme.axis.lineThickness,
                            labelRotation = layout.xAxis.style?.labelRotation ?: 0,
                        ),
                    ),
                    yAxisContent = AxisContent(
                        labels = {
                            if (layout.yAxis.isLabels) {
                                AxisLabel(it.toString(), Modifier.absolutePadding(right = 2.dp))
                            }
                        },
                        title = {
                            if (layout.yAxis.isTitle) {
                                Box(
                                    modifier = Modifier.fillMaxHeight(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AxisTitle(
                                        layout.yAxis.title,
                                        modifier = paddingMod
                                            .rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                                    )
                                }
                            }
                        },
                        style = AxisStyle(
                            color = layout.yAxis.style?.color ?: KoalaPlotTheme.axis.color,
                            majorTickSize = layout.yAxis.style?.majorTickSize
                                ?: KoalaPlotTheme.axis.majorTickSize,
                            minorTickSize = layout.yAxis.style?.minorTickSize
                                ?: KoalaPlotTheme.axis.minorTickSize,
                            tickPosition = layout.yAxis.style?.tickPosition
                                ?: KoalaPlotTheme.axis.xyGraphTickPosition,
                            lineWidth = layout.yAxis.style?.lineWidth
                                ?: KoalaPlotTheme.axis.lineThickness,
                            labelRotation = layout.yAxis.style?.labelRotation ?: 0,
                        )
                    ),
                    gridStyle  = rememberGridStyle(
                        horizontalMajorStyle = layout.gridStyle?.horizontalMajorStyle ?: KoalaPlotTheme.axis.majorGridlineStyle,
                        horizontalMinorStyle = layout.gridStyle?.horizontalMinorStyle ?: KoalaPlotTheme.axis.minorGridlineStyle,
                        verticalMajorStyle = layout.gridStyle?.verticalMajorStyle ?: KoalaPlotTheme.axis.majorGridlineStyle,
                        verticalMinorStyle = layout.gridStyle?.verticalMinorStyle ?: KoalaPlotTheme.axis.minorGridlineStyle
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {

                    when (layout.type) {
                        ChartType.Line -> {
                            val scope = this as XYGraphScope<Double, Float>
                            scope.LineChart(data, layout.tooltips.isTooltips, layout.tooltips.isSymbol)

                        }
                        ChartType.VerticalBar -> {
                            val scope = this as XYGraphScope<String, Float>
                            scope.VerticalBarChart(data, layout.tooltips.isTooltips,layout.barConf.widthWeight  )
                        }


                        ChartType.BoxPlot -> {
                            val scope = this as XYGraphScope<String, Float>
                            scope.BoxPlot(data, layout.tooltips.isTooltips )
                        }

                        ChartType.Geo -> {
                            val scope = this as XYGraphScope<Double, Double>
                            scope.GeoChart(data,layout.tooltips.isTooltips )
                        }

                        ChartType.XYGraph -> TODO()
                        else -> {}
                    }
                }

            }
        } //-- ChartLayout

        if (layout.caption.isCaption) {
            Box( modifier = Modifier.fillMaxSize(),
                contentAlignment = layout.caption.location
            ) {
                CaptionText(layout.caption.title, modifier = paddingMod)
            }
        } //-- Caption

    }
}



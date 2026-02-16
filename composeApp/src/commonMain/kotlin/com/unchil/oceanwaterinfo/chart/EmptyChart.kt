package com.unchil.oceanwaterinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.legend.LegendLocation
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.AxisContent
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberAxisStyle


@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun EmptyChart(layoutData: LayoutData){

    Box( modifier = Modifier.fillMaxWidth()
        .height(layoutData.size.height),
        contentAlignment =  Alignment.Center
    ) {

        ChartLayout(
            modifier = Modifier.padding(16.dp),
            title = {
                if(layoutData.layout.isTitle) {
                    ChartTitle(layoutData.layout.title)
                }
            },
            legend = {},
            legendLocation = LegendLocation.LEFT
        ) {
            XYGraph(
                xAxisModel = FloatLinearAxisModel(
                    0f..10f,
                    minimumMajorTickSpacing = 50.dp,
                ),
                yAxisModel = FloatLinearAxisModel(
                    0f..10f,
                    minimumMajorTickSpacing = 50.dp,
                ),
                xAxisContent =
                    AxisContent(
                        labels = {
                            AxisLabel(it.toString(), Modifier.padding(top = 2.dp))
                        },
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                if(layoutData.xAxis.isTitle){
                                    AxisTitle(layoutData.xAxis.title)
                                }
                            }
                        },
                        style = rememberAxisStyle(),
                    ),
                yAxisContent =
                    AxisContent(
                        labels = {
                            AxisLabel(it.toString(), Modifier.absolutePadding(right = 2.dp))
                        },
                        title = {
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.Center,
                            ) {
                                if(layoutData.yAxis.isTitle){
                                    AxisTitle(
                                        layoutData.yAxis.title,
                                        modifier = Modifier
                                            .rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                                            .padding(bottom = padding),
                                    )
                                }
                            }
                        },
                        style = rememberAxisStyle(),
                    )
            ) {

            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment =  Alignment.BottomEnd
        ) {
            if(layoutData.caption.isCaption){
                CaptionText(layoutData.caption.title, modifier = paddingMod)
            }

        }
    }

}

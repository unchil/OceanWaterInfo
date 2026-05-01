package com.unchil.oceanwaterinfo.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.CaptionText
import com.unchil.oceanwaterinfo.ChartData
import com.unchil.oceanwaterinfo.ChartTitle
import com.unchil.oceanwaterinfo.ChartType
import com.unchil.oceanwaterinfo.LayoutData
import com.unchil.oceanwaterinfo.Legend
import com.unchil.oceanwaterinfo.description
import com.unchil.oceanwaterinfo.getColors
import com.unchil.oceanwaterinfo.paddingMod
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.polar.AngularAxisModel
import io.github.koalaplot.core.polar.PolarGraph
import io.github.koalaplot.core.polar.PolarGraphDefaults
import io.github.koalaplot.core.polar.PolarPlotSeries2
import io.github.koalaplot.core.polar.rememberAngularValueAxisModel
import io.github.koalaplot.core.polar.rememberFloatRadialAxisModel
import io.github.koalaplot.core.style.AreaStyle
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.toDegrees
import kotlin.math.round

@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PolarPlotChart(
    layout: LayoutData,
    chartData: ChartData,
    entries:List<String>
){


    Column (
        modifier = paddingMod.fillMaxWidth(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val colors = getColors(entries)

       // if (!layout.layout.description.isNullOrBlank()) description

        Box(  contentAlignment =  Alignment.Center ) {

            ChartLayout(
                modifier = paddingMod
                    .sizeIn(minHeight = layout.size.minHeight, maxHeight = layout.size.maxHeight)
                    .background(color = MaterialTheme.colorScheme.surface),
                title = { if (layout.layout.isTitle) ChartTitle(layout.layout.title, modifier = paddingMod) },
                legend = { if(layout.legend.isUsable )  Legend(layout, entries, colors) },
                legendLocation = layout.legend.location
            ){

                val angularAxisGridLineStyle =
                    LineStyle(SolidColor(Color.LightGray), strokeWidth = 1.dp)

                PolarGraph(
                    radialAxisModel = rememberFloatRadialAxisModel(
                        List(5) { i -> round((layout.maxCrSp / 3) * i  ) }
                    ),
                    angularAxisModel = rememberAngularValueAxisModel(
                        angleDirection = AngularAxisModel.AngleDirection.CLOCKWISE ,
                        angleZero = AngularAxisModel.AngleZero.TWELVE_OCLOCK
                    ),
                    radialAxisLabels = {
                        Text("${it}" )
                    },
                    angularAxisLabels = {
                        Text("${it.toDegrees().value}\u00B0")
                    },
                    polarGraphProperties = PolarGraphDefaults.polarGraphPropertyDefaults()
                        .copy(
                            angularAxisGridLineStyle = angularAxisGridLineStyle,
                            radialAxisGridLineStyle = angularAxisGridLineStyle,
                            background = AreaStyle(
                                SolidColor(Color.Yellow),
                                alpha = 0.1f,
                            ),
                        ),
                ) {

                    PolarChart(chartData, layout.tooltips.isTooltips, layout.tooltips.isSymbol, )

                }

                if (layout.caption.isCaption) {
                    Box( modifier = Modifier.fillMaxSize(),
                        contentAlignment = layout.caption.location
                    ) {
                        CaptionText(layout.caption.title, modifier = paddingMod)
                    }
                }

            }
        }
    }
}
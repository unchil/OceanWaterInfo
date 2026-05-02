package com.unchil.oceanwaterinfo.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.unchil.oceanwaterinfo.caption
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

    Box( modifier = Modifier
        .fillMaxWidth()
        .height(layout.size.height),
        contentAlignment =  Alignment.Center
    ) {

        ChartLayout(
            modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.surface),
            title = { if (layout.layout.isTitle) ChartTitle(layout.layout.title,) },
            legend = { if(layout.legend.isUsable )  Legend(layout, entries) },
            legendLocation = layout.legend.location
        ){

            Column (
                modifier = paddingMod.fillMaxWidth().height(layout.size.height),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {


                if (!layout.layout.description.isNullOrBlank()) description(layout.layout.description)

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

            }

        }// ChartLayout

        if (layout.caption.isCaption) caption(layout.caption.title, layout.caption.location) //-- Caption

    }
}
@file:Suppress("MagicNumber")

package com.unchil.oceanwaterinfo


import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.legend.LegendLocation
import io.github.koalaplot.core.util.generateHueColorPalette
import io.github.koalaplot.core.xygraph.AxisModel
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.GridStyle
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.TickPosition
import kotlin.Any
import kotlin.math.ceil

typealias ObservationtDataType = List< Triple< String, Point<Double, Double>, Pair<String, Float>> >
typealias GeoShapeDataType = List<Point<Double,Double>>
typealias GeoChartDataType =  Triple< List<String>,  ObservationtDataType , GeoShapeDataType >

typealias ChartEntriesType = List<String>

sealed class ChartUiState {

    object Loading : ChartUiState()

    data class Success(
        val chartData: Any,
        val entries: ChartEntriesType,
        val chartLayout: LayoutData,
        val geoData: GeoShapeDataType? = null
    ) : ChartUiState()

    data class EmptyChart(
        val chartLayout: LayoutData,
        val geoData: GeoShapeDataType? = null
    ) : ChartUiState()

    data class Error(
        val message: String
    ) : ChartUiState()
}


// 1. 차트 제목 및 레이아웃 설정
data class TitleConfig(
    val isTitle: Boolean = false,
    val title: String = "",
    val description:String? = null
)

// 2. 범례 설정
data class LegendConfig(
    val isUsable: Boolean = false,
    val isTitle: Boolean = false,
    val title: String = "",
    val location: LegendLocation = LegendLocation.RIGHT
)

// 3. 축(Axis) 설정
data class AxisConfig(
    val title: String = "",
    val isTitle: Boolean = true,
    val isLabels: Boolean = true,
    val style: AxisStyle? = null, // 아래에서 기본값 할당
    val model: Any? = null
)

// 4. 캡션 설정
data class CaptionConfig(
    val isCaption: Boolean = false,
    val title: String = "",
    val location: Alignment = Alignment.BottomEnd
)

// 5. 툴팁 및 크기 설정
data class TooltipConfig(
    val isTooltips: Boolean = true,
    val isSymbol: Boolean = false,
)

data class SizeConfig(
    val height: Dp = 400.dp,
    val minHeight: Dp = 200.dp,
    val maxHeight: Dp = height,
)

data class BarConfig(val widthWeight: Float = 0.8f)



data class LayoutData(

    val type: ChartType = ChartType.XYGraph,
    val layout: TitleConfig = TitleConfig(),
    val legend: LegendConfig = LegendConfig(),
    val xAxis: AxisConfig = AxisConfig(
        style = AxisStyle(
            color = Color.LightGray,
            majorTickSize = 0.dp,
            minorTickSize = 0.dp,
            tickPosition = TickPosition.None,
            lineWidth = 1.dp,
            labelRotation = 0
        )
    ),
    val yAxis: AxisConfig = AxisConfig(
        style = AxisStyle(
            color = Color.LightGray,
            majorTickSize = 0.dp,
            minorTickSize = 0.dp,
            tickPosition = TickPosition.None,
            lineWidth = 1.dp,
            labelRotation = 0
        )
    ),
    val gridStyle: GridStyle? = null,
    val caption: CaptionConfig = CaptionConfig(),
    val tooltips: TooltipConfig = TooltipConfig(),
    val size: SizeConfig = SizeConfig(),
    val barConf: BarConfig = BarConfig()
) {

}

enum class ChartType {
    XYGraph, Line, VerticalBar, GroupVerticalBar, BoxPlot, Geo
}

enum class BoxPlotRange {
    MIN_MAX, Q1_Q3, Q2, MIN, MAX
}

val padding = 8.dp
val paddingMod = Modifier.padding(padding)



object WATER_QUALITY {

    const val caption =  "해양수산부(Ministry of Oceans and Fisheries) https://www.mof.go.kr"
    enum class QualityType{
        rtmWtchWtem, rtmWqCndctv, ph, rtmWqDoxn, rtmWqTu, rtmWqChpla, rtmWqSlnty
    }
    fun QualityType.name():String{
        return when(this) {
            /*
            QualityType.rtmWtchWtem -> "수온"
            QualityType.rtmWqCndctv -> "전기전도도"
            QualityType.ph -> "수소이온농도"
            QualityType.rtmWqDoxn -> "용존산소량"
            QualityType.rtmWqTu -> "탁도"
            QualityType.rtmWqChpla -> "클로로필"
            QualityType.rtmWqSlnty -> "염분"

             */
            QualityType.rtmWtchWtem -> "Water Temperature"
            QualityType.rtmWqCndctv -> "Electrical Conductivity"
            QualityType.ph -> "Hydrogen Ion Concentration"
            QualityType.rtmWqDoxn -> "Dissolved Oxygen"
            QualityType.rtmWqTu -> "Turbidity"
            QualityType.rtmWqChpla -> "Chlorophyll"
            QualityType.rtmWqSlnty -> "Salinity"
        }
    }

    fun QualityType.desc():String{
        return when(this) {

            QualityType.rtmWtchWtem -> "양식에 적합한 해양 수질 온도는 기르는 어종에 따라 크게 다르며, " +
                    "생물의 건강한 성장과 생존을 위해 매우 중요한 요소입니다. " +
                    "일반적으로는 겨울철 12℃ 이상, 여름철 28℃ 이하를 유지하는 것이 좋습니다. 주요 양식 어종별 적정 수온은 " +
                    "넙치:21~24℃, 조피볼락(우럭):12~21℃, 뱀장어:25~26℃, 바지락:15~22℃, 전복: 15~20℃, 돔류(참돔, 감성돔, 돌돔): 저수온에 약하며 생존 가능 최적수온은 6~7℃.  "
            QualityType.rtmWqCndctv -> "해수의 전기전도도는 약 50 mS/cm (밀리시멘스/센티미터) 또는 50,000 μS/cm (마이크로지멘스/센티미터)입니다. " +
                    "이는 담수보다 훨씬 높으며, 해수의 염분 함량, 온도, 압력 등 여러 요인에 의해 영향을 받습니다. " +
                    "해수의 높은 전기전도도는 물에 녹아 있는 다량의 이온 때문이며, 전기전도도를 측정하여 해수의 염분 농도를 추정할 수 있습니다. "
            QualityType.ph -> "해수의 수소이온농도는 일반적으로 약알칼리성을 띠며 pH 7.9 ~ 8.1 정도입니다. " +
                    "대기 중 이산화탄소 증가로 해수가 이산화탄소를 흡수하면서 해수의 수소이온농도가 증가하고 pH가 낮아지는 현상인 해양 산성화가 진행 중입니다. " +
                    "이로 인해 해수의 pH는 점차 낮아지고 있으며, 해양 생태계에 영향을 미칩니다."
            QualityType.rtmWqDoxn -> "해수 용존 산소량은 표층(수심 100m 이내)에서 광합성과 대기 중 산소 용해로 인해 가장 많으며, " +
                    "수심이 깊어질수록 호흡 작용과 사체 분해로 감소하다가, 극지방의 찬 해수가 심층으로 내려가면서 다시 증가합니다. " +
                    "또한, 수온이 낮고 염분이 낮을수록, 그리고 기압이 높을수록 용존 산소량이 많아집니다"
            QualityType.rtmWqTu -> "해수 수질 탁도는 물에 부유한 입자 때문에 물이 얼마나 흐린지를 나타내는 지표로, 주로 빛을 산란시키는 정도를 측정합니다. " +
                    "이는 해양 생태계와 수질에 큰 영향을 미치며, 측정 단위는 주로 NTU (Nephelometric Turbidity Unit)를 사용합니다. " +
                    "해수의 탁도를 높이는 요인으로는 다양한 부유물질과 염분이 있으며, 정확한 측정을 위해서는 부식에 강하고 고정밀 측정이 가능한 센서가 필요합니다. "
            QualityType.rtmWqChpla -> "해수 클로로필(엽록소)은 해양 생태계의 일차 생산력을 나타내는 지표로, 주로 클로로필-a를 측정하며, 식물플랑크톤의 양과 관련이 있습니다. " +
                    "해수 클로로필 농도를 측정하기 위해 용매를 이용한 흡광광도법이나 형광 측정법, 또는 위성 관측 등을 활용하며, " +
                    "이 수치는 해수의 수질 및 영양 상태를 파악하는 데 중요하게 사용됩니다."
            QualityType.rtmWqSlnty -> "바다의 평균 염분 농도는 약 3.5%이며, 바닷물 1kg당 염분이 35g 녹아있습니다. " +
                    "이는 1,000에 대한 비율로 나타내는 35‰(퍼밀)로 표시되며, 염분 농도가 가장 높은 주요 원인은 소금의 주성분인 염화나트륨입니다. " +
                    "바닷물의 염분 농도는 지역에 따라 다르며, 대양의 경우 일반적으로 33~37‰입니다"


            /*
            QualityType.rtmWtchWtem -> "The optimal ocean water temperature for aquaculture varies greatly depending on the species being raised, and is a crucial factor for the healthy growth and survival of the organisms. \n" +
                    "Generally, it is recommended to maintain a temperature above 12℃ in winter and below 28℃ in summer. The optimal water temperature for each major aquaculture species is \n" +
                    "Fluffy: 21-24℃, Black Rockfish (Rockfish): 12-21℃, Eel: 25-26℃, Clam: 15-22℃, Abalone: 15-20℃, Sea Breams (Red Sea Bream, Black Sea Bream, Rock Bream): are sensitive to low temperatures and the optimal water temperature for survival is 6-7℃."
            QualityType.rtmWqCndctv -> "The electrical conductivity of seawater is approximately 50 mS/cm (millisiemens/centimeter) or 50,000 μS/cm (microsiemens/centimeter). \n" +
                    "This is much higher than freshwater and is affected by several factors, including the salt content, temperature, and pressure of seawater. \n" +
                    "The high electrical conductivity of seawater is due to the large amount of ions dissolved in it, and measuring electrical conductivity can be used to estimate the salinity of seawater."
            QualityType.ph -> "The hydrogen ion concentration of seawater is generally slightly alkaline, with a pH of around 7.9 to 8.1. \n" +
                    "Ocean acidification, a phenomenon in which seawater absorbs carbon dioxide due to increased atmospheric carbon dioxide, increases the hydrogen ion concentration and lowers the pH, is occurring. \n" +
                    "This is gradually lowering the pH of seawater, affecting marine ecosystems."
            QualityType.rtmWqDoxn -> "Dissolved oxygen in seawater is highest at the surface (within 100 meters of water depth) due to photosynthesis and atmospheric oxygen dissolution. \n" +
                    "As water depth increases, it decreases due to respiration and decomposition of dead organisms, then increases again as the cold polar waters sink to the depths. \n" +
                    "Furthermore, dissolved oxygen increases with lower water temperature and salinity, and with higher atmospheric pressure."
            QualityType.rtmWqTu -> "Seawater turbidity is an indicator of how cloudy the water is due to suspended particles. It primarily measures the degree to which light is scattered. \n" +
                    "This has a significant impact on marine ecosystems and water quality, and is typically measured in NTU (Nephelometric Turbidity Units). \n" +
                    "Factors that increase seawater turbidity include various suspended solids and salinity. Accurate measurement requires a corrosion-resistant, high-precision sensor."
            QualityType.rtmWqChpla -> "Seawater chlorophyll (chlorophyll) is an indicator of the primary productivity of marine ecosystems. It primarily measures chlorophyll-a and is related to the amount of phytoplankton. \n" +
                    "Seawater chlorophyll concentration is measured using solvent-based spectrophotometry, fluorescence measurements, or satellite observations. \n" +
                    "This value is crucial for understanding seawater quality and nutritional status."
            QualityType.rtmWqSlnty -> "The average salinity of the ocean is approximately 3.5%, meaning 35 grams of salt are dissolved in 1 kg of seawater. \n" +
                    "This is expressed as 35‰ (parts per thousand), and the main cause of high salinity is sodium chloride, the main component of salt. \n" +
                    "Seawater salinity varies by region, but in the open ocean, it is typically between 33 and 37‰."

             */
        }
    }

    fun QualityType.unit():String{
        return when(this) {
            /*
            QualityType.rtmWtchWtem -> "℃ (섭씨)"
            QualityType.rtmWqCndctv -> "mS/cm (밀리시멘스 퍼 센티미터)"
            QualityType.ph -> "pH"
            QualityType.rtmWqDoxn -> "mg/L"
            QualityType.rtmWqTu -> "NTU(Nephelometric Turbidity Unit)"
            QualityType.rtmWqChpla -> "mg/m³(밀리그램/세제곱미터)"
            QualityType.rtmWqSlnty -> "‰(퍼밀)"

             */
            QualityType.rtmWtchWtem -> "℃ (Celsius)"
            QualityType.rtmWqCndctv -> "mS/cm (millisiemens per centimeter)"
            QualityType.ph -> "pH"
            QualityType.rtmWqDoxn -> "mg/L"
            QualityType.rtmWqTu -> "NTU (Nephelometric Turbidity Unit)"
            QualityType.rtmWqChpla -> "mg/m³ (milligrams per cubic meter)"
            QualityType.rtmWqSlnty -> "‰ (permille)"
        }
    }


}




val getColors = { entries:List<String> ->
    buildMap {
        val colors = generateHueColorPalette(entries.size)
        entries.sortedBy { it }.forEachIndexed { index, it ->
            put(it, colors[index])
        }
    }
}



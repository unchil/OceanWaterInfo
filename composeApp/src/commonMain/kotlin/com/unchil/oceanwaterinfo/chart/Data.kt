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
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.GridStyle
import io.github.koalaplot.core.xygraph.TickPosition
import kotlin.Any


sealed class ChartUiState {

    object Loading : ChartUiState()

    data class Success(
        val chartData:  ChartData,
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
    val barConf: BarConfig = BarConfig(),
    var maxCrSp:Float = 0f
) {

}

enum class ChartGraphScope{
    XY, Polar
}

enum class ChartType {
    XYGraph, Line, VerticalBar, GroupVerticalBar, BoxPlot, Geo, DegLine, Point, Area, Polar
}

enum class BoxPlotRange {
    MIN_MAX, Q1_Q3, Q2, MIN, MAX
}

val padding = 8.dp
val paddingMod = Modifier.padding(padding)


const val WasteWaterDescription = "해양 산성화(Ocean Acidification):\n" +
        "\t\t\t흔히 '기후 변화의 쌍둥이 악당'이라고 불릴 만큼 해양 생태계에 치명적인 영향을 미칩니다.\n" +
        "1. 해양 산성화의 화학적 원리\n" +
        "\t\t\t기본적으로 바닷물은 약알칼리성(평균 pH 8.1 정도)을 띱니다. 그러나 대기 중 CO_2 농도가 높아지면 다음과 같은 화학 반응이 일어납니다.\n" +
        "\t\t\t\t\t\t1. 용해: 대기 중의 CO_2가 바다 표면으로 흡수됩니다.\n" +
        "\t\t\t\t\t\t2. 탄산 형성: CO_2가 물(H_2O)과 반응하여 탄산(H_2CO_3)을 형성합니다.\n" +
        "\t\t\t\t\t\t3. 수소 이온 방출: 탄산이 수소 이온(H^+)과 탄산수소 이온(HCO_3^-)으로 해리됩니다.\n" +
        "\t\t\t\t\t\t4. pH 하락: 이때 방출된 수소 이온(H^+)의 농도가 높아지면서 바닷물의 산성도가 강해집니다.\n" +
        "2. 생태계에 미치는 영향\n" +
        "\t\t\t\t\t\t해양 산성화의 가장 큰 피해자는 탄산칼슘(CaCO_3)으로 껍질이나 골격을 만드는 생물들입니다.\n" +
        "\t\t\t\t\t\t• 산호초의 붕괴: 바다의 열대우림이라 불리는 산호초가 약해지면, 이곳을 보금자리로 삼는 수많은 해양 생물의 서식처가 사라집니다.\n" +
        "\t\t\t\t\t\t• 감각 및 행동 변화: 산성도가 높아지면 일부 어류는 포식자의 냄새를 맡거나 소리를 듣는 능력이 저하되어 생존 확률이 낮아진다는 연구 결과도 있습니다.\n" +
        "3. 우리 삶에 미치는 파급 효과\n" +
        "\t\t\t바다는 인류가 배출한 이산화탄소의 약 25~30%를 흡수하며 지구 온난화를 늦춰주는 완충 작용을 해왔습니다. 하지만 산성화가 임계치를 넘으면 다음과 같은 문제가 발생합니다.\n" +
        "\t\t\t\t\t\t• 수산업 위축: 굴, 게, 새우 등 주요 수산자원의 생산량이 줄어들어 식량 안보와 경제에 타격을 줍니다.\n" +
        "\t\t\t\t\t\t• 탄소 흡수 능력 저하: 바다가 산성화될수록 추가적인 CO_2를 흡수하는 능력이 떨어져, 결국 대기 중 온난화를 가속화하는 악순환에 빠질 수 있습니다.\n" +
        "4. 현재 상황과 미래 전망\n" +
        "\t\t\t산업화 이전 바다의 평균 pH는 약 8.2였으나, 현재는 약 8.1로 낮아졌습니다. 숫자로 보면 미미해 보이지만, pH는 로그 스케일이기 때문에 산성도가 약 30% 증가했음을 의미합니다.\n" +
        "\t\t\t현재와 같은 추세로 이산화탄소가 배출된다면, 2100년경에는 pH가 7.7~7.8까지 떨어질 것으로 예측됩니다. 이는 지난 수천만 년 동안 겪어보지 못한 급격한 변화로, 해양 생물들이 적응하기에는 시간이 턱없이 부족한 상황입니다.\n" +
        "\t\t\t참고: 해양 산성화를 막는 근본적인 해결책은 탄소 배출을 줄이는 것뿐입니다. 더불어 해조류(미역, 다시마 등) 양식을 통해 국소적으로 CO_2를 흡수하거나, 해양 보호 구역을 설정하여 생태계의 복원력을 높이는 노력이 병행되고 있습니다.\n " +
        "\nGemini's answer to the question, \"Tell me more about ocean acidification.\""


object AIR_QUAlITY_UNION {
    const val caption =  "https://data.seoul.go.kr https://data.gg.go.kr"
    enum class QualityType{
        o3, no2, co, so2, nh3, h2s, pm10, pm25
    }
    fun AIR_QUAlITY_UNION.QualityType.name():String {
        return when (this) {
            QualityType.no2 -> "Nitrogen dioxide(NO2)" //이산화질소
            QualityType.co -> "Carbon monoxide(CO)" // 일산화탄소
            QualityType.so2 -> "Sulfur dioxide(SO2)"  // 이산화황
            QualityType.nh3 -> "Ammonia/Hydrogen Nitride(NH3)" // 암모니아
            QualityType.h2s -> "Hydrogen sulfide(H2S)" // 황화수소
            QualityType.o3 -> "Ozone(O3)" // 오존
            QualityType.pm10 -> "Particulate Matter(PM10)"
            QualityType.pm25 -> "Particulate Matter(PM2.5)"
        }
    }

    fun AIR_QUAlITY_UNION.QualityType.desc():String{
        return when(this) {
            QualityType.no2 -> "이산화질소(NO2)는 주로 자동차 배기가스나 화력 발전소 등 연소 과정에서 발생하는 적갈색의 독성 기체입니다. " +
                    "오존과 마찬가지로 물에 잘 녹지 않아 폐 깊숙한 곳까지 침투하며, 농도에 따라 다음과 같은 영향을 미칩니다.\n " +
                    "냄새: 갓 뽑아낸 독한 매연 또는 찌릿한 염소(수영장) 냄새 \n" +
                    "[0.1 ~ 0.2]: 냄새를 감지할 수 있는 수준이며, 민감한 사람의 경우 호흡기 자극을 느낌.\n" +
                    "[0.5 ~ 1.0]: 짧은 노출에도 기도가 수축하기 시작하며, 호흡기 저항이 증가함.\n" +
                    "[1.0 ~ 3.0]: 주의 단계. 건강한 성인도 폐 기능의 유의미한 저하를 경험하며 코, 목의 통증 유발.\n" +
                    "[5.0 ~ 10.0]: 위험 단계. 심한 기침과 가슴 통증이 나타나며, 수 분간 노출 시 기관지염 유발 가능.\n" +
                    "[50 ~ 100]: 치명적 단계. 단시간 노출로도 폐포 손상 및 폐수종(폐에 물이 참) 발생 가능.\n" +
                    "[150 이상]: 매우 짧은 시간 노출만으로도 사망에 이를 수 있는 극독성 수준. \n\n" +
                    "폐질환자에게 미치는 영향: \n" +
                    "이산화질소는 산화 스트레스를 유발하여 기도 염증을 악화시키기 때문에 폐가 약한 분들에게는 훨씬 낮은 농도에서도 위험합니다.\n"+
                    "천식 환자: 기도의 과민성을 높입니다. 즉, 평소에는 괜찮았던 꽃가루나 먼지 같은 알레르기 유발 물질에 폐가 훨씬 더 격렬하게 반응하게 만듭니다. 0.2~0.3ppm 정도의 낮은 농도에서도 천식 발작 빈도가 높아질 수 있습니다.\n"+
                    "만성폐쇄성폐질환(COPD): 기도의 가래 배출 능력을 저하시키고 세균 감염에 대한 저항력을 약화시킵니다. 이는 감기나 폐렴 같은 2차 감염으로 이어져 병세가 급격히 악화되는 원인이 됩니다.\n" +
                    "어린이 및 영유아: 폐가 완전히 발달하지 않은 상태에서 이산화질소에 지속적으로 노출되면 성인이 된 후에도 폐 기능이 정상보다 낮아지는 '폐 성장 저해'가 발생할 수 있습니다."
            QualityType.co -> "일산화탄소(CO)는 '소리 없는 살인자'라고 불릴 만큼 무색, 무취, 무미의 특성을 가집니다. 폐를 통해 혈액으로 들어간 CO는 산소를 운반하는 헤모글로빈(Hb)과 결합하여 **일산화탄소-헤모글로빈(COHb)**을 형성하는데, 그 결합력이 산소보다 약 200~250배 강해 체내 산소 공급을 차단합니다. \n" +
                    "냄새: 무색, 무취 (냄새가 전혀 없음) \n" +
                    "[50]: 허용 한계치(8시간 기준). 장시간 노출 시 가벼운 두통 가능성.\n" +
                    "[200]: 2~3시간 내에 가벼운 두통, 피로, 어지러움 발생.\n" +
                    "[400]: 1~2시간 내에 전두부 두통 및 메스꺼움. 3시간 후 생명 위험.\n" +
                    "[800]: 위험 단계. 45분 내에 현기증, 구토, 경련. 2시간 내 의식 불명.\n" +
                    "[1,600]: 심각 단계. 20분 내에 두통 및 어지러움. 2시간 내 사망 가능성.\n" +
                    "[3,200]: 5~10분 내에 머리가 어지럽고 30분 내에 사망.\n" +
                    "[12,800]: 1~3분 이내에 즉사. \n\n" +
                    "폐질환자 및 심혈관 질환자에게 미치는 피해:\n" +
                    "일산화탄소는 폐 자체를 파괴하기보다 '가스 교환'의 효율성을 무너뜨립니다. 이미 폐 기능이 떨어진 환자에게는 소량의 CO도 치명적입니다.\n" +
                    "산소 부족의 가속화: 만성폐쇄성폐질환(COPD)이나 폐기종 환자는 이미 혈중 산소 포화도가 낮습니다. 여기에 CO가 유입되면 남아있는 산소 운반 능력마저 마비되어 급성 호흡 부전이 올 수 있습니다.\n" +
                    "심장 과부하: 산소가 부족해지면 심장은 더 많은 혈액을 보내기 위해 과하게 뜁니다. 폐가 약해 심장에 무리가 가 있는 상태(폐성심 등)에서 CO에 노출되면 심근경색이나 부정맥이 발생할 위험이 매우 높습니다. \n" +
                    "낮은 농도에서의 민감성: 건강한 성인은 50ppm에서 큰 증상을 느끼지 못할 수 있지만, 폐 질환자는 같은 농도에서도 심한 가슴 통증이나 호흡 곤란을 겪을 수 있습니다."
            QualityType.so2 -> "이산화황(SO2)은 물에 매우 잘 녹는 수용성 기체로, 흡입 시 상기도(코, 목)의 점막에 즉각적으로 흡수되어 황산을 형성하며 강한 자극을 일으킵니다. 특히 대기 중 미세먼지와 결합하여 폐 깊숙이 침투할 경우 그 피해가 더 커집니다. \n" +
                    "냄새: 금방 끈 성냥의 탄내 또는 화약 냄새 \n" +
                    "[0.1 ~ 0.5]: 민감한 사람이나 천식 환자가 장시간 노출 시 호흡 곤란을 느끼기 시작함.\n" +
                    "[1.0 ~ 2.0]: 건강한 성인도 불쾌한 냄새를 느끼고, 눈과 목에 가벼운 자극 발생.\n" +
                    "[3.0 ~ 5.0]: 주의 단계. 코와 목에 강한 자극, 마른 기침이 나며 호흡 저항이 증가함.\n" +
                    "[10.0 ~ 20.0]: 위험 단계. 눈 점막의 심한 통증, 재채기, 목 쉼, 가슴 압박감이 즉각적으로 나타남.\n" +
                    "[50.0 ~ 100]: 심각 단계. 30분~1시간 노출 시 생명이 위험할 수 있으며 폐부종 및 기관지염 발생.\n" +
                    "[400 ~ 500]: 짧은 노출만으로도 질식 및 폐의 심각한 손상으로 사망 가능.\n\n" +
                    "폐질환자(천식, 만성기관지염 등)에게 미치는 피해:\n" +
                    "이산화황은 모든 가스 중 폐질환자에게 가장 즉각적이고 직접적인 고통을 주는 물질 중 하나입니다.\n" +
                    "기도 수축(Bronchoconstriction): 이산화황은 기도를 둘러싼 근육을 수축시킵니다. 천식 환자는 일반인보다 10~20배 더 민감하게 반응하며, 단 5~10분간의 짧은 노출(0.5ppm 수준)만으로도 심한 호흡 곤란을 겪을 수 있습니다.\n" +
                    "만성기관지염 환자: 점액(가래) 분비를 비정상적으로 증가시켜 기도를 폐쇄합니다. 이로 인해 기침이 멈추지 않고 산소 공급이 원활하지 않아 심부전으로 이어질 위험이 있습니다.\n" +
                    "상조 효과: 이산화황이 대기 중 수분과 만나 산성 안개(Acid Mist)나 초미세먼지(황산염) 형태로 변하면, 폐포 말단까지 도달하여 조직을 직접적으로 부식시키고 염증을 일으킵니다."
            QualityType.nh3 -> "암모니아(NH3)는 강한 알칼리성을 띠는 부식성 가스로, 물에 매우 잘 녹는 성질이 있습니다. 흡입 시 호흡기 점막의 수분과 결합하여 강염기인 수산화암모늄(NH4OH)을 형성하며 조직을 녹여내는 화학적 화상을 입힙니다.\n " +
                    "냄새: 오래 방치된 공중화장실 또는 삭힌 홍어 냄새 \n" +
                    "[5 ~ 20]: 냄새를 감지할 수 있는 수준. 대부분의 건강한 사람에게는 무해함.\n" +
                    "[25 ~ 50]: 노출 기준(8시간). 눈, 코, 목에 가벼운 자극이 시작됨.\n" +
                    "[100]: 눈과 상기도에 뚜렷한 자극. 30분 이상 노출 시 눈물과 기침 유발.\n" +
                    "[300 ~ 500]: 주의 단계. 즉각적인 코와 목의 통증. 호흡 시 가슴 답답함과 가쁜 숨.\n" +
                    "[1,700 ~ 2,500]: 위험 단계. 30분 노출 시 심각한 후두 부종 및 기관지 경련. 생명 위협.\n" +
                    "[5,000 ~ 10,000]: 치명적 단계. 단시간 노출로도 호흡 정지 및 질식 사망 가능.\n\n" +
                    "폐질환자에게 미치는 피해\n" +
                    "암모니아는 조직을 부식시키는 성질이 강해, 폐 기능이 이미 손상된 환자에게는 회복 불가능한 타격을 줄 수 있습니다.\n" +
                    "화학적 폐렴의 위험: 암모니아가 폐포까지 도달하면 상피 세포를 파괴하여 '화학적 폐렴'을 유발합니다. 폐기종이나 결핵 병력이 있는 환자는 조직 재생 능력이 떨어져 일반인보다 훨씬 심각한 염증 반응을 겪게 됩니다.\n" +
                    "급성 기도 폐쇄: 천식이나 COPD 환자는 암모니아 가스의 자극에 반응하여 기도가 급격히 부어오릅니다. 이는 공기 통로를 즉각적으로 차단하여 급성 호흡 부전을 일으키는 주요 원인이 됩니다.\n" +
                    "이차 감염 취약성: 암모니아 노출로 인해 기도의 점막과 섬모(먼지를 걸러내는 털)가 손상되면 외부 세균이나 바이러스에 대한 방어막이 사라집니다. 폐질환자는 이로 인해 치명적인 2차 세균성 폐렴으로 이어질 확률이 매우 높습니다."
            QualityType.h2s -> "황화수소(H2S)는 달걀 썩는 냄새가 나는 무색의 독성 기체로, 화산 가스나 하수구, 폐수 처리장 등 유기물이 부패하는 곳에서 주로 발생합니다. 이 기체는 세포의 호흡 기전(세포 내 산소 이용)을 마비시키기 때문에 '화학적 질식제'로 분류됩니다. 저농도에서는 냄새로 감지되지만, 농도가 높아지면 후각 신경을 마비시켜 냄새를 맡지 못하게 하므로 매우 기만적이고 위험합니다.\n" +
                    "냄새: 상한 달걀(썩은 달걀) 냄새 \n" +
                    "[0.01 ~ 0.3]: 특유의 달걀 썩는 냄새를 감지함.\n" +
                    "[10]: 허용 한계치(8시간 기준). 눈의 가벼운 자극.\n" +
                    "[50 ~ 100]: 눈과 기도에 심한 자극, 결막염, 기침, 1시간 노출 시 소화기 장애 및 현기증.\n" +
                    "[150 ~ 250]: 후각 마비 발생. 더 이상 냄새가 나지 않아 안전하다고 착각하게 됨 (매우 위험).\n" +
                    "[320 ~ 530]: 위험 단계. 폐부종 발생 가능성, 30분~1시간 노출 시 생명 위험.\n" +
                    "[530 ~ 1,000]: 심각 단계. 신경계 마비, 빈맥(빠른 심박), 근육 경련 및 의식 불명.\n" +
                    "[1,000 이상]: 치명적 단계. 단 한 번의 흡입('Knock-down')으로도 즉사 가능. \n\n" +
                    "폐질환자에게 미치는 피해:\n" +
                    "황화수소는 폐포 상피 세포에 직접적인 손상을 주며, 체내 산소 대사를 방해하므로 폐 기능이 저하된 환자에게는 소량으로도 치명적입니다.\n" +
                    "급성 폐수종 유발: 황화수소는 폐혈관의 투과성을 높여 폐에 물이 차게 만듭니다. 만성폐쇄성폐질환(COPD)이나 심부전이 있는 환자는 혈액 내 산소가 이미 부족한 상태에서 폐수종이 겹치면 급격한 질식 상태에 빠집니다.\n" +
                    "기도 과민성 증폭: 천식 환자의 경우, 매우 낮은 농도(2~5ppm)에서도 기도가 경련을 일으키며 수축할 수 있습니다. 이는 일반적인 대기 오염 물질보다 훨씬 강력한 기폭제가 됩니다.\n" +
                    "조직 재생 저해: 황화수소의 독성은 폐 조직의 미토콘드리아 기능을 억제합니다. 이미 폐 섬유화가 진행 중이거나 조직이 약해진 환자는 노출 후 폐 기능 회복이 거의 불가능할 정도로 손상될 수 있습니다."
            QualityType.o3 -> "오존(O3)은 강력한 산화력을 가진 기체로, 농도에 따라 호흡기 및 심혈관 계통에 상당한 자극을 줍니다. " +
                    "특히 폐 기능이 이미 저하된 환자들에게는 낮은 농도에서도 치명적일 수 있습니다. \n" +
                    "냄새: 복사기/프린터 근처의 비릿한 냄새 또는 소나기 직후의 상쾌한 듯 비릿한 풀내음 \n" +
                    "[0.01 ~ 0.03]: 대기 중 일반적인 배경 농도로, 대다수가 감지하지 못함. \n" +
                    "[0.1]: 불쾌한 냄새를 느끼기 시작하며, 눈과 코의 점막에 가벼운 자극 발생. \n" +
                    "[0.1 ~ 0.3]: 주의 단계. 시력 저하, 두통, 가슴 압박감이 나타날 수 있으며 운동 시 숨 가쁨 현상 발생. \n"+
                    "[0.5 ~ 1.0]: 위험 단계. 마른 기침, 상기도 건조함, 심한 피로감을 느낌. 2시간 노출 시 폐 기능이 유의미하게 감소함. \n" +
                    "[1.0 ~ 3.0]: 심각 단계. 1~2시간 노출 시 기관지염, 폐부종(폐에 물이 참) 가능성. 폐렴 증세와 유사한 통증 유발. \n" +
                    "[10.0]: 이상단시간 노출로도 의식 불명 및 사망에 이를 수 있는 매우 위험한 수준.\n\n" +
                    "폐질환자(천식, COPD 등)에게 미치는 피해:\n" +
                    "폐 건강이 취약한 분들에게 오존은 단순한 자극을 넘어 **질환의 악화(Exacerbation)**를 유발하는 기폭제가 됩니다.\n" +
                    "천식 환자: 오존은 기도를 수축시키고 염증 반응을 일으킵니다. 일반인에게는 무해한 0.05ppm 수준에서도 천식 환자는 기침과 쌕쌕거림(천명)이 심해지며, 평소 복용하는 약물의 효과가 떨어질 수 있습니다.\n" +
                    "만성폐쇄성폐질환(COPD) 환자: 오존 노출 시 폐의 산소 교환 능력이 급격히 떨어집니다. 이는 호흡 곤란을 심화시켜 응급실 방문이나 입원 가능성을 크게 높입니다.\n" +
                    "영구적인 손상 위험: 반복적인 고농도 노출은 폐 조직의 섬유화(딱딱해짐)를 초래할 수 있는데, 기저 질환이 있는 경우 이러한 구조적 변화가 더 빠르게 진행될 수 있습니다."
            QualityType.pm10 -> "PM10 (미세먼지): 입자의 지름이 10 마이크로미터 이하인 먼지입니다. 사람의 머리카락 굵기(약 50 ~ 70 마이크로미터)보다 5~7배 정도 작습니다.\n" +
                    "코털이나 기관지 점막에서 어느 정도 걸러지지만, 과다 노출 시 상기도 염증, 기침, 천식을 유발할 수 있습니다.\n" +
                    "발생 원인: \n" +
                    "미세먼지는 발생하는 방식에 따라 두 가지로 나뉩니다.\n" +
                    "1차적 발생: 공장, 자동차, 건설 현장에서 직접 배출되거나 흙먼지, 꽃가루와 같이 자연적으로 발생하는 경우입니다.\n" +
                    "2차적 발생: 대기 중으로 배출된 황산화물(SO_x), 질소산화물(NO_x), 암모니아(NH_3) 등이 화학 반응을 일으켜 미세먼지로 변하는 경우입니다.\n" +
                    "수도권 초미세먼지의 상당 부분이 이 2차 발생에 해당합니다.\n" +
                    "좋음: 0~30\n" +
                    "보통: 31~80\n" +
                    "나쁨: 81~ 150\n" +
                    "매우나쁨: 151~"
            QualityType.pm25 -> "PM2.5 (초미세먼지): 입자의 지름이 2.5 마이크로미터 이하인 먼지입니다. 머리카락 굵기보다 20~30배 정도 작아 눈에 보이지 않을 정도로 매우 미세합니다.\n" +
                    "크기가 너무 작아 폐포(허파꽈리)까지 직접 도달하며, 혈관을 타고 전신으로 퍼질 수 있습니다.\n" +
                    "이는 심혈관계 질환, 뇌졸중, 암(1군 발암물질)의 원인이 되기도 합니다.\n" +
                    "발생 원인: \n" +
                    "미세먼지는 발생하는 방식에 따라 두 가지로 나뉩니다.\n" +
                    "1차적 발생: 공장, 자동차, 건설 현장에서 직접 배출되거나 흙먼지, 꽃가루와 같이 자연적으로 발생하는 경우입니다.\n" +
                    "2차적 발생: 대기 중으로 배출된 황산화물(SO_x), 질소산화물(NO_x), 암모니아(NH_3) 등이 화학 반응을 일으켜 미세먼지로 변하는 경우입니다.\n" +
                    "수도권 초미세먼지의 상당 부분이 이 2차 발생에 해당합니다.\n" +
                    "좋음: 0~15\n" +
                    "보통: 16~35\n" +
                    "나쁨: 36~ 75\n" +
                    "매우나쁨: 76~"

        }
    }

}
object AIR_QUAlITY {
    const val caption =  "https://data.seoul.go.kr/dataList/OA-15969/S/1/datasetView.do (Seoul Metropolitan Government)"
    enum class QualityType{
        max_o3, max_no2, max_co, max_so2, max_nh3, max_h2s
    }
    fun AIR_QUAlITY.QualityType.name():String {
        return when (this) {
            QualityType.max_no2 -> "Nitrogen dioxide(NO2)" //이산화질소
            QualityType.max_co -> "Carbon monoxide(CO)" // 일산화탄소
            QualityType.max_so2 -> "Sulfur dioxide(SO2)"  // 이산화황
            QualityType.max_nh3 -> "Ammonia/Hydrogen Nitride(NH3)" // 암모니아
            QualityType.max_h2s -> "Hydrogen sulfide(H2S)" // 황화수소
            QualityType.max_o3 -> "Ozone(O3)" // 오존
        }
    }

    fun AIR_QUAlITY.QualityType.desc():String{
        return when(this) {
            QualityType.max_no2 -> "이산화질소(NO2)는 주로 자동차 배기가스나 화력 발전소 등 연소 과정에서 발생하는 적갈색의 독성 기체입니다. " +
                    "오존과 마찬가지로 물에 잘 녹지 않아 폐 깊숙한 곳까지 침투하며, 농도에 따라 다음과 같은 영향을 미칩니다.\n " +
                    "냄새: 갓 뽑아낸 독한 매연 또는 찌릿한 염소(수영장) 냄새 \n" +
                    "[0.1 ~ 0.2]: 냄새를 감지할 수 있는 수준이며, 민감한 사람의 경우 호흡기 자극을 느낌.\n" +
                    "[0.5 ~ 1.0]: 짧은 노출에도 기도가 수축하기 시작하며, 호흡기 저항이 증가함.\n" +
                    "[1.0 ~ 3.0]: 주의 단계. 건강한 성인도 폐 기능의 유의미한 저하를 경험하며 코, 목의 통증 유발.\n" +
                    "[5.0 ~ 10.0]: 위험 단계. 심한 기침과 가슴 통증이 나타나며, 수 분간 노출 시 기관지염 유발 가능.\n" +
                    "[50 ~ 100]: 치명적 단계. 단시간 노출로도 폐포 손상 및 폐수종(폐에 물이 참) 발생 가능.\n" +
                    "[150 이상]: 매우 짧은 시간 노출만으로도 사망에 이를 수 있는 극독성 수준. \n\n" +
                    "폐질환자에게 미치는 영향: \n" +
                    "이산화질소는 산화 스트레스를 유발하여 기도 염증을 악화시키기 때문에 폐가 약한 분들에게는 훨씬 낮은 농도에서도 위험합니다.\n"+
                    "천식 환자: 기도의 과민성을 높입니다. 즉, 평소에는 괜찮았던 꽃가루나 먼지 같은 알레르기 유발 물질에 폐가 훨씬 더 격렬하게 반응하게 만듭니다. 0.2~0.3ppm 정도의 낮은 농도에서도 천식 발작 빈도가 높아질 수 있습니다.\n"+
                    "만성폐쇄성폐질환(COPD): 기도의 가래 배출 능력을 저하시키고 세균 감염에 대한 저항력을 약화시킵니다. 이는 감기나 폐렴 같은 2차 감염으로 이어져 병세가 급격히 악화되는 원인이 됩니다.\n" +
                    "어린이 및 영유아: 폐가 완전히 발달하지 않은 상태에서 이산화질소에 지속적으로 노출되면 성인이 된 후에도 폐 기능이 정상보다 낮아지는 '폐 성장 저해'가 발생할 수 있습니다."
            QualityType.max_co -> "일산화탄소(CO)는 '소리 없는 살인자'라고 불릴 만큼 무색, 무취, 무미의 특성을 가집니다. 폐를 통해 혈액으로 들어간 CO는 산소를 운반하는 헤모글로빈(Hb)과 결합하여 **일산화탄소-헤모글로빈(COHb)**을 형성하는데, 그 결합력이 산소보다 약 200~250배 강해 체내 산소 공급을 차단합니다. \n" +
                    "냄새: 무색, 무취 (냄새가 전혀 없음) \n" +
                    "[50]: 허용 한계치(8시간 기준). 장시간 노출 시 가벼운 두통 가능성.\n" +
                    "[200]: 2~3시간 내에 가벼운 두통, 피로, 어지러움 발생.\n" +
                    "[400]: 1~2시간 내에 전두부 두통 및 메스꺼움. 3시간 후 생명 위험.\n" +
                    "[800]: 위험 단계. 45분 내에 현기증, 구토, 경련. 2시간 내 의식 불명.\n" +
                    "[1,600]: 심각 단계. 20분 내에 두통 및 어지러움. 2시간 내 사망 가능성.\n" +
                    "[3,200]: 5~10분 내에 머리가 어지럽고 30분 내에 사망.\n" +
                    "[12,800]: 1~3분 이내에 즉사. \n\n" +
                    "폐질환자 및 심혈관 질환자에게 미치는 피해:\n" +
                    "일산화탄소는 폐 자체를 파괴하기보다 '가스 교환'의 효율성을 무너뜨립니다. 이미 폐 기능이 떨어진 환자에게는 소량의 CO도 치명적입니다.\n" +
                    "산소 부족의 가속화: 만성폐쇄성폐질환(COPD)이나 폐기종 환자는 이미 혈중 산소 포화도가 낮습니다. 여기에 CO가 유입되면 남아있는 산소 운반 능력마저 마비되어 급성 호흡 부전이 올 수 있습니다.\n" +
                    "심장 과부하: 산소가 부족해지면 심장은 더 많은 혈액을 보내기 위해 과하게 뜁니다. 폐가 약해 심장에 무리가 가 있는 상태(폐성심 등)에서 CO에 노출되면 심근경색이나 부정맥이 발생할 위험이 매우 높습니다. \n" +
                    "낮은 농도에서의 민감성: 건강한 성인은 50ppm에서 큰 증상을 느끼지 못할 수 있지만, 폐 질환자는 같은 농도에서도 심한 가슴 통증이나 호흡 곤란을 겪을 수 있습니다."
            QualityType.max_so2 -> "이산화황(SO2)은 물에 매우 잘 녹는 수용성 기체로, 흡입 시 상기도(코, 목)의 점막에 즉각적으로 흡수되어 황산을 형성하며 강한 자극을 일으킵니다. 특히 대기 중 미세먼지와 결합하여 폐 깊숙이 침투할 경우 그 피해가 더 커집니다. \n" +
                    "냄새: 금방 끈 성냥의 탄내 또는 화약 냄새 \n" +
                    "[0.1 ~ 0.5]: 민감한 사람이나 천식 환자가 장시간 노출 시 호흡 곤란을 느끼기 시작함.\n" +
                    "[1.0 ~ 2.0]: 건강한 성인도 불쾌한 냄새를 느끼고, 눈과 목에 가벼운 자극 발생.\n" +
                    "[3.0 ~ 5.0]: 주의 단계. 코와 목에 강한 자극, 마른 기침이 나며 호흡 저항이 증가함.\n" +
                    "[10.0 ~ 20.0]: 위험 단계. 눈 점막의 심한 통증, 재채기, 목 쉼, 가슴 압박감이 즉각적으로 나타남.\n" +
                    "[50.0 ~ 100]: 심각 단계. 30분~1시간 노출 시 생명이 위험할 수 있으며 폐부종 및 기관지염 발생.\n" +
                    "[400 ~ 500]: 짧은 노출만으로도 질식 및 폐의 심각한 손상으로 사망 가능.\n\n" +
                    "폐질환자(천식, 만성기관지염 등)에게 미치는 피해:\n" +
                    "이산화황은 모든 가스 중 폐질환자에게 가장 즉각적이고 직접적인 고통을 주는 물질 중 하나입니다.\n" +
                    "기도 수축(Bronchoconstriction): 이산화황은 기도를 둘러싼 근육을 수축시킵니다. 천식 환자는 일반인보다 10~20배 더 민감하게 반응하며, 단 5~10분간의 짧은 노출(0.5ppm 수준)만으로도 심한 호흡 곤란을 겪을 수 있습니다.\n" +
                    "만성기관지염 환자: 점액(가래) 분비를 비정상적으로 증가시켜 기도를 폐쇄합니다. 이로 인해 기침이 멈추지 않고 산소 공급이 원활하지 않아 심부전으로 이어질 위험이 있습니다.\n" +
                    "상조 효과: 이산화황이 대기 중 수분과 만나 산성 안개(Acid Mist)나 초미세먼지(황산염) 형태로 변하면, 폐포 말단까지 도달하여 조직을 직접적으로 부식시키고 염증을 일으킵니다."
            QualityType.max_nh3 -> "암모니아(NH3)는 강한 알칼리성을 띠는 부식성 가스로, 물에 매우 잘 녹는 성질이 있습니다. 흡입 시 호흡기 점막의 수분과 결합하여 강염기인 수산화암모늄(NH4OH)을 형성하며 조직을 녹여내는 화학적 화상을 입힙니다.\n " +
                    "냄새: 오래 방치된 공중화장실 또는 삭힌 홍어 냄새 \n" +
                    "[5 ~ 20]: 냄새를 감지할 수 있는 수준. 대부분의 건강한 사람에게는 무해함.\n" +
                    "[25 ~ 50]: 노출 기준(8시간). 눈, 코, 목에 가벼운 자극이 시작됨.\n" +
                    "[100]: 눈과 상기도에 뚜렷한 자극. 30분 이상 노출 시 눈물과 기침 유발.\n" +
                    "[300 ~ 500]: 주의 단계. 즉각적인 코와 목의 통증. 호흡 시 가슴 답답함과 가쁜 숨.\n" +
                    "[1,700 ~ 2,500]: 위험 단계. 30분 노출 시 심각한 후두 부종 및 기관지 경련. 생명 위협.\n" +
                    "[5,000 ~ 10,000]: 치명적 단계. 단시간 노출로도 호흡 정지 및 질식 사망 가능.\n\n" +
                    "폐질환자에게 미치는 피해\n" +
                    "암모니아는 조직을 부식시키는 성질이 강해, 폐 기능이 이미 손상된 환자에게는 회복 불가능한 타격을 줄 수 있습니다.\n" +
                    "화학적 폐렴의 위험: 암모니아가 폐포까지 도달하면 상피 세포를 파괴하여 '화학적 폐렴'을 유발합니다. 폐기종이나 결핵 병력이 있는 환자는 조직 재생 능력이 떨어져 일반인보다 훨씬 심각한 염증 반응을 겪게 됩니다.\n" +
                    "급성 기도 폐쇄: 천식이나 COPD 환자는 암모니아 가스의 자극에 반응하여 기도가 급격히 부어오릅니다. 이는 공기 통로를 즉각적으로 차단하여 급성 호흡 부전을 일으키는 주요 원인이 됩니다.\n" +
                    "이차 감염 취약성: 암모니아 노출로 인해 기도의 점막과 섬모(먼지를 걸러내는 털)가 손상되면 외부 세균이나 바이러스에 대한 방어막이 사라집니다. 폐질환자는 이로 인해 치명적인 2차 세균성 폐렴으로 이어질 확률이 매우 높습니다."
            QualityType.max_h2s -> "황화수소(H2S)는 달걀 썩는 냄새가 나는 무색의 독성 기체로, 화산 가스나 하수구, 폐수 처리장 등 유기물이 부패하는 곳에서 주로 발생합니다. 이 기체는 세포의 호흡 기전(세포 내 산소 이용)을 마비시키기 때문에 '화학적 질식제'로 분류됩니다. 저농도에서는 냄새로 감지되지만, 농도가 높아지면 후각 신경을 마비시켜 냄새를 맡지 못하게 하므로 매우 기만적이고 위험합니다.\n" +
                    "냄새: 상한 달걀(썩은 달걀) 냄새 \n" +
                    "[0.01 ~ 0.3]: 특유의 달걀 썩는 냄새를 감지함.\n" +
                    "[10]: 허용 한계치(8시간 기준). 눈의 가벼운 자극.\n" +
                    "[50 ~ 100]: 눈과 기도에 심한 자극, 결막염, 기침, 1시간 노출 시 소화기 장애 및 현기증.\n" +
                    "[150 ~ 250]: 후각 마비 발생. 더 이상 냄새가 나지 않아 안전하다고 착각하게 됨 (매우 위험).\n" +
                    "[320 ~ 530]: 위험 단계. 폐부종 발생 가능성, 30분~1시간 노출 시 생명 위험.\n" +
                    "[530 ~ 1,000]: 심각 단계. 신경계 마비, 빈맥(빠른 심박), 근육 경련 및 의식 불명.\n" +
                    "[1,000 이상]: 치명적 단계. 단 한 번의 흡입('Knock-down')으로도 즉사 가능. \n\n" +
                    "폐질환자에게 미치는 피해:\n" +
                    "황화수소는 폐포 상피 세포에 직접적인 손상을 주며, 체내 산소 대사를 방해하므로 폐 기능이 저하된 환자에게는 소량으로도 치명적입니다.\n" +
                    "급성 폐수종 유발: 황화수소는 폐혈관의 투과성을 높여 폐에 물이 차게 만듭니다. 만성폐쇄성폐질환(COPD)이나 심부전이 있는 환자는 혈액 내 산소가 이미 부족한 상태에서 폐수종이 겹치면 급격한 질식 상태에 빠집니다.\n" +
                    "기도 과민성 증폭: 천식 환자의 경우, 매우 낮은 농도(2~5ppm)에서도 기도가 경련을 일으키며 수축할 수 있습니다. 이는 일반적인 대기 오염 물질보다 훨씬 강력한 기폭제가 됩니다.\n" +
                    "조직 재생 저해: 황화수소의 독성은 폐 조직의 미토콘드리아 기능을 억제합니다. 이미 폐 섬유화가 진행 중이거나 조직이 약해진 환자는 노출 후 폐 기능 회복이 거의 불가능할 정도로 손상될 수 있습니다."
            QualityType.max_o3 -> "오존(O3)은 강력한 산화력을 가진 기체로, 농도에 따라 호흡기 및 심혈관 계통에 상당한 자극을 줍니다. " +
                    "특히 폐 기능이 이미 저하된 환자들에게는 낮은 농도에서도 치명적일 수 있습니다. \n" +
                    "냄새: 복사기/프린터 근처의 비릿한 냄새 또는 소나기 직후의 상쾌한 듯 비릿한 풀내음 \n" +
                    "[0.01 ~ 0.03]: 대기 중 일반적인 배경 농도로, 대다수가 감지하지 못함. \n" +
                    "[0.1]: 불쾌한 냄새를 느끼기 시작하며, 눈과 코의 점막에 가벼운 자극 발생. \n" +
                    "[0.1 ~ 0.3]: 주의 단계. 시력 저하, 두통, 가슴 압박감이 나타날 수 있으며 운동 시 숨 가쁨 현상 발생. \n"+
                    "[0.5 ~ 1.0]: 위험 단계. 마른 기침, 상기도 건조함, 심한 피로감을 느낌. 2시간 노출 시 폐 기능이 유의미하게 감소함. \n" +
                    "[1.0 ~ 3.0]: 심각 단계. 1~2시간 노출 시 기관지염, 폐부종(폐에 물이 참) 가능성. 폐렴 증세와 유사한 통증 유발. \n" +
                    "[10.0]: 이상단시간 노출로도 의식 불명 및 사망에 이를 수 있는 매우 위험한 수준.\n\n" +
                    "폐질환자(천식, COPD 등)에게 미치는 피해:\n" +
                    "폐 건강이 취약한 분들에게 오존은 단순한 자극을 넘어 **질환의 악화(Exacerbation)**를 유발하는 기폭제가 됩니다.\n" +
                    "천식 환자: 오존은 기도를 수축시키고 염증 반응을 일으킵니다. 일반인에게는 무해한 0.05ppm 수준에서도 천식 환자는 기침과 쌕쌕거림(천명)이 심해지며, 평소 복용하는 약물의 효과가 떨어질 수 있습니다.\n" +
                    "만성폐쇄성폐질환(COPD) 환자: 오존 노출 시 폐의 산소 교환 능력이 급격히 떨어집니다. 이는 호흡 곤란을 심화시켜 응급실 방문이나 입원 가능성을 크게 높입니다.\n" +
                    "영구적인 손상 위험: 반복적인 고농도 노출은 폐 조직의 섬유화(딱딱해짐)를 초래할 수 있는데, 기저 질환이 있는 경우 이러한 구조적 변화가 더 빠르게 진행될 수 있습니다."

        }
    }

}
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



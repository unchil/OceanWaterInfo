package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.ChartDataFlowTimeSeries
import com.unchil.oceanwaterinfo.ChartType
import com.unchil.oceanwaterinfo.toChartTripleList
import com.unchil.oceanwaterinfo.viewmodel.KhnpThermalWasteWaterViewModel
import com.unchil.oceanwaterinfo.viewmodel.KhnpWasteWaterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes


@Composable
fun WasteWaterTimeSeries_KHNP() {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhnpWasteWaterViewModel = remember { KhnpWasteWaterViewModel(coroutineScope) }
    val onRefresh:()->Unit = {
        coroutineScope.launch {
            while(true){
                delay(1 * 60 * 1000L).let{
                    viewModel.onEvent(KhnpWasteWaterViewModel.Event.Refresh)
                }
            }
        }
    }

    val wasterWaterInfo = viewModel._khnpWasteWaterStateFlow.collectAsState()

    val chartData = wasterWaterInfo.value.filter { item ->
        val previousHour = kotlin.time.Clock.System.now()
            .minus(3, DateTimeUnit.HOUR)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toInstant(TimeZone.UTC)

        val checkTime_Wastewater = 10.minutes

        val time = LocalDateTime.parse(item.time.replace(" ", "T")).toInstant(TimeZone.UTC)
        val tm01 = LocalDateTime.parse(item.tm001_time.replace(" ", "T")).toInstant(TimeZone.UTC)
        val tm02 = LocalDateTime.parse(item.tm002_time.replace(" ", "T")).toInstant(TimeZone.UTC)

        time >= previousHour &&
                (time - tm01).absoluteValue <= checkTime_Wastewater &&
                (time - tm02).absoluteValue <= checkTime_Wastewater

    }.toChartTripleList(
        nameSelector = { it.genName },
        timeSelector = { it.time },
        timePattern = "yyyy-MM-dd HH:mm",
        primaryValueSelector = { it.tm002.trim().toFloatOrNull() ?: 0f },
        secondaryValueSelector = { it.tm001.trim().toFloatOrNull() ?: 0f }, // 유량
        secondaryKey = "tm001"
    )

    if(chartData.isNotEmpty()){
        ChartDataFlowTimeSeries(
            chartData = chartData,
            title = "3-hour WasteWater Current",
            xTitle = "DateTime",
            yTitle = "Quality(PH)",
            caption = "from https://www.data.go.kr/data/15157700/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.Point,
            yRangePadding = 0.1f,
            // YAxis min/max 에 함께 사용될 secondaryKey
        //    secondaryKey = "tm001",
            onRefresh = onRefresh
        ){
            Text(
                text = "해양 산성화(Ocean Acidification)\n" +
                        "흔히 '기후 변화의 쌍둥이 악당'이라고 불릴 만큼 해양 생태계에 치명적인 영향을 미칩니다.\n" +
                        "\n1. 해양 산성화의 화학적 원리\n" +
                        "기본적으로 바닷물은 약알칼리성(평균 pH 8.1 정도)을 띱니다. 그러나 대기 중 CO_2 농도가 높아지면 다음과 같은 화학 반응이 일어납니다.\n" +
                        "\t\t\t1. 용해: 대기 중의 CO_2가 바다 표면으로 흡수됩니다.\n" +
                        "\t\t\t2. 탄산 형성: CO_2가 물(H_2O)과 반응하여 탄산(H_2CO_3)을 형성합니다.\n" +
                        "\t\t\t3. 수소 이온 방출: 탄산이 수소 이온(H^+)과 탄산수소 이온(HCO_3^-)으로 해리됩니다.\n" +
                        "\t\t\t4. pH 하락: 이때 방출된 수소 이온(H^+)의 농도가 높아지면서 바닷물의 산성도가 강해집니다.\n" +
                        "\n2. 생태계에 미치는 영향\n" +
                        "해양 산성화의 가장 큰 피해자는 탄산칼슘(CaCO_3)으로 껍질이나 골격을 만드는 생물들입니다.\n" +
                        "\t\t\t• 석회화 생물의 고통: 산호, 조개, 소라, 게, 그리고 먹이사슬의 기초가 되는 날개고동(Sea Butterfly) 등은 탄산수소 이온이 늘어나면 껍질을 만들기 어려워집니다. 심할 경우 이미 만들어진 껍질이 산성 환경에서 녹아내리기도 합니다.\n" +
                        "\t\t\t• 산호초의 붕괴: 바다의 열대우림이라 불리는 산호초가 약해지면, 이곳을 보금자리로 삼는 수많은 해양 생물의 서식처가 사라집니다.\n" +
                        "\t\t\t• 감각 및 행동 변화: 산성도가 높아지면 일부 어류는 포식자의 냄새를 맡거나 소리를 듣는 능력이 저하되어 생존 확률이 낮아진다는 연구 결과도 있습니다.\n" +
                        "\n3. 우리 삶에 미치는 파급 효과\n" +
                        "바다는 인류가 배출한 이산화탄소의 약 25~30%를 흡수하며 지구 온난화를 늦춰주는 완충 작용을 해왔습니다. 하지만 산성화가 임계치를 넘으면 다음과 같은 문제가 발생합니다.\n" +
                        "\t\t\t• 수산업 위축: 굴, 게, 새우 등 주요 수산자원의 생산량이 줄어들어 식량 안보와 경제에 타격을 줍니다.\n" +
                        "\t\t\t• 탄소 흡수 능력 저하: 바다가 산성화될수록 추가적인 CO_2를 흡수하는 능력이 떨어져, 결국 대기 중 온난화를 가속화하는 악순환에 빠질 수 있습니다.\n" +
                        "\n4. 현재 상황과 미래 전망\n" +
                        "산업화 이전 바다의 평균 pH는 약 8.2였으나, 현재는 약 8.1로 낮아졌습니다. 숫자로 보면 미미해 보이지만, pH는 로그 스케일이기 때문에 산성도가 약 30% 증가했음을 의미합니다.\n" +
                        "현재와 같은 추세로 이산화탄소가 배출된다면, 2100년경에는 pH가 7.7~7.8까지 떨어질 것으로 예측됩니다. 이는 지난 수천만 년 동안 겪어보지 못한 급격한 변화로, 해양 생물들이 적응하기에는 시간이 턱없이 부족한 상황입니다.\n" +
                        "참고: 해양 산성화를 막는 근본적인 해결책은 탄소 배출을 줄이는 것뿐입니다. 더불어 해조류(미역, 다시마 등) 양식을 통해 국소적으로 CO_2를 흡수하거나, 해양 보호 구역을 설정하여 생태계의 복원력을 높이는 노력이 병행되고 있습니다.\n " +
                        "Gemini's answer to the question, \"Tell me more about ocean acidification.\"",
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                textAlign = TextAlign.Start
            )

        }
    }




}
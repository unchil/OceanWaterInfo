package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.AirQualityManager.airQualityStageComment
import com.unchil.oceanwaterinfo.AirQualityManager.airQualityStageRange
import com.unchil.oceanwaterinfo.AirQualityManager.nameEn
import com.unchil.oceanwaterinfo.AirQualityManager.specialFeature
import com.unchil.oceanwaterinfo.AirQualityManager.unHealthyForSensitiveGroups
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoUnionViewModel
import kotlinx.coroutines.delay

@Composable
fun SDoTDescription(
    sDoTEnvInfo: List<SDoTEnvInfoUnion>,
    selectedOption: AirQualityManager.ChemicalElement,
     splitFractionVertical: Float
){


    val caption = "미국 환경보호청(US EPA)의 공식 가이드라인을 바탕으로, 공기질 항목(EPA 기준 오염물질 6종 + 산업/안전 가스 2종)을 6단계로 분류"
    val unHealthyForSensitive =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
    val unHealthy =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
    val veryUnHealthy =  remember{ mutableListOf<Triple< AirQualityManager.AirQualityStage, Float, String>>()}
    val hazardous =  remember{ mutableListOf<Triple<AirQualityManager.AirQualityStage, Float, String>>()}
    val values = remember{ mutableStateOf("" )}
    val maxValue = remember{ mutableStateOf(0.0 )}
    val sDoTEnvInfoStat = remember{ mutableStateOf(emptyList<Pair<AirQualityManager.AirQualityStage, Int>>())}

    LaunchedEffect( sDoTEnvInfo, key2=selectedOption){

        unHealthyForSensitive.clear()
        unHealthy.clear()
        veryUnHealthy.clear()
        hazardous.clear()

        if(sDoTEnvInfo.isNotEmpty()) {
            values.value = sDoTEnvInfo.map{it}.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) { it ->
                val value = when (selectedOption) {
                    AirQualityManager.ChemicalElement.o3 -> it.o3.toFloatOrNull() ?: 0f
                    AirQualityManager.ChemicalElement.no2 -> it.no2.toFloatOrNull() ?: 0f
                    AirQualityManager.ChemicalElement.co -> it.co.toFloatOrNull() ?: 0f
                    AirQualityManager.ChemicalElement.so2 -> it.so2.toFloatOrNull() ?: 0f
                    AirQualityManager.ChemicalElement.nh3 -> it.nh3.toFloatOrNull() ?: 0f
                    AirQualityManager.ChemicalElement.h2s -> it.h2s.toFloatOrNull() ?: 0f
                    AirQualityManager.ChemicalElement.pm10 -> it.pm10.toFloatOrNull() ?: 0f
                    AirQualityManager.ChemicalElement.pm25 -> it.pm25.toFloatOrNull() ?: 0f
                }

                if(value > 0f) {
                    val airQualityStage =
                        AirQualityManager.calculateTotalStage(value.toDouble(), selectedOption)

                    when (airQualityStage) {
                        AirQualityManager.AirQualityStage.UNHEALTHY_FOR_SENSITIVE -> {
                            unHealthyForSensitive.add(Triple(airQualityStage, value, it.addr))
                        }

                        AirQualityManager.AirQualityStage.UNHEALTHY -> {
                            unHealthy.add(Triple(airQualityStage, value, it.addr))
                        }
                        AirQualityManager.AirQualityStage.VERY_UNHEALTHY -> {
                            veryUnHealthy.add(Triple(airQualityStage, value, it.addr))
                        }
                        AirQualityManager.AirQualityStage.HAZARDOUS -> {
                            hazardous.add(Triple(airQualityStage, value, it.addr))
                        }

                        else -> {}
                    }
                }

                "{ sensing_time:\"${it.sensing_time}\", obs:\"${it.obs}\", lat:${it.lat}, lng:${it.lng},  addr:\"${it.addr}\", value:${value} }"
            }

            maxValue.value  =
                if (sDoTEnvInfo.isEmpty()) 0.0
                else sDoTEnvInfo.map { sensor ->
                    val v = when (selectedOption) {
                        AirQualityManager.ChemicalElement.o3 -> sensor.o3
                        AirQualityManager.ChemicalElement.no2 -> sensor.no2
                        AirQualityManager.ChemicalElement.co -> sensor.co
                        AirQualityManager.ChemicalElement.so2 -> sensor.so2
                        AirQualityManager.ChemicalElement.nh3 -> sensor.nh3
                        AirQualityManager.ChemicalElement.h2s -> sensor.h2s
                        AirQualityManager.ChemicalElement.pm10 -> sensor.pm10
                        AirQualityManager.ChemicalElement.pm25 -> sensor.pm25
                    }
                    v.toDoubleOrNull() ?: 0.0
                }.max()

            sDoTEnvInfoStat.value = sDoTEnvInfo.groupBy { sensor ->

                val v = when (selectedOption) {
                    AirQualityManager.ChemicalElement.o3 -> sensor.o3
                    AirQualityManager.ChemicalElement.no2 -> sensor.no2
                    AirQualityManager.ChemicalElement.co -> sensor.co
                    AirQualityManager.ChemicalElement.so2 -> sensor.so2
                    AirQualityManager.ChemicalElement.nh3 -> sensor.nh3
                    AirQualityManager.ChemicalElement.h2s -> sensor.h2s
                    AirQualityManager.ChemicalElement.pm10 -> sensor.pm10
                    AirQualityManager.ChemicalElement.pm25 -> sensor.pm25
                }.toDoubleOrNull() ?: 0.0
                AirQualityManager.calculateTotalStage(v, selectedOption)
            }.map{ ( airQualityStage, group) ->
                Pair( airQualityStage ,  group.size)
            }.sortedBy{
                it.first.level
            }
        }

    }


    Box(
        modifier = Modifier.fillMaxWidth(
             splitFractionVertical
        ).fillMaxHeight()
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = selectedOption.nameEn(),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )


            val airQualityStage =
                AirQualityManager.calculateTotalStage(maxValue.value, selectedOption)

            Spacer(Modifier.padding(2.dp))

            AirQualityStatusBoard(airQualityStage, sDoTEnvInfoStat.value)

            caption(caption, Alignment.Center)

            Spacer(Modifier.padding(2.dp))

            AnimatedVisibility(hazardous.isNotEmpty()) {
                val color = AirQualityManager.AirQualityStage.HAZARDOUS.argbColor

                val hazardousText = hazardous.sortedWith(compareByDescending { it.second })
                    .mapIndexed { index, triple ->
                        val number = (index + 1).toString().padStart(2, '0') // 01, 02... 형태
                        "$number | ${triple.second} | ${triple.third}"
                    }
                    .joinToString("\n")

                OutlinedTextField(
                    value = hazardousText,
                    onValueChange = {}, // 읽기 전용
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    label = { Text("위험" )   },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = color,
                        focusedBorderColor = color,
                        focusedLabelColor = color,
                        unfocusedLabelColor = color,
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )


            }

            Spacer(Modifier.padding(2.dp))

            AnimatedVisibility(veryUnHealthy.isNotEmpty()) {
                val color = AirQualityManager.AirQualityStage.VERY_UNHEALTHY.argbColor

                val veryUnhealthyText = veryUnHealthy.sortedWith(compareByDescending { it.second })
                    .mapIndexed { index, triple ->
                        val number = (index + 1).toString().padStart(2, '0') // 01, 02... 형태
                        "$number | ${triple.second} | ${triple.third}"
                    }
                    .joinToString("\n")

                OutlinedTextField(
                    value = veryUnhealthyText,
                    onValueChange = {}, // 읽기 전용
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    label = { Text("매우 나쁨") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = color,
                        focusedBorderColor = color,
                        focusedLabelColor = color,
                        unfocusedLabelColor = color,
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }

            Spacer(Modifier.padding(2.dp))

            AnimatedVisibility(unHealthy.isNotEmpty()) {
                val color = AirQualityManager.AirQualityStage.UNHEALTHY.argbColor

                val unhealthyText = unHealthy.sortedWith(compareByDescending { it.second })
                    .mapIndexed { index, triple ->
                        val number = (index + 1).toString().padStart(2, '0') // 01, 02... 형태
                        "$number | ${triple.second} | ${triple.third}"
                    }
                    .joinToString("\n")

                OutlinedTextField(
                    value = unhealthyText,
                    onValueChange = {}, // 읽기 전용
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    label = { Text("나쁨") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = color,
                        focusedBorderColor = color,
                        focusedLabelColor = color,
                        unfocusedLabelColor = color,
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }

            Spacer(Modifier.padding(2.dp))

            AnimatedVisibility(unHealthyForSensitive.isNotEmpty()) {
                val color = AirQualityManager.AirQualityStage.UNHEALTHY_FOR_SENSITIVE.argbColor

                val unHealthyForSensitiveText = unHealthyForSensitive.sortedWith(compareByDescending { it.second })
                    .mapIndexed { index, triple ->
                        val number = (index + 1).toString().padStart(2, '0') // 01, 02... 형태
                        "$number | ${triple.second} | ${triple.third}"
                    }
                    .joinToString("\n")

                OutlinedTextField(
                    value = unHealthyForSensitiveText,
                    onValueChange = {}, // 읽기 전용
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    label = { Text("민감군 영향") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = color,
                        focusedBorderColor = color,
                        focusedLabelColor = color,
                        unfocusedLabelColor = color,
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }


            Spacer(Modifier.padding(2.dp))


            BasicTextField(
                value = "Information",
                onValueChange = { },
                readOnly = true,
                decorationBox = { innerTextField ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {

                        Box( modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                            innerTextField()
                        }


                        Spacer(Modifier.padding(2.dp))

                        OutlinedTextField(
                            value = selectedOption.specialFeature(),
                            onValueChange = {}, // 읽기 전용
                            readOnly = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            label = { Text("특징") },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.padding(2.dp))

                        OutlinedTextField(
                            value = selectedOption.unHealthyForSensitiveGroups(),
                            onValueChange = {}, // 읽기 전용
                            readOnly = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            label = { Text("민감군 영향") },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AirQualityManager.AirQualityStage.entries.forEachIndexed { index, stage ->
                            val range = selectedOption.airQualityStageRange()[index]
                            val comment = selectedOption.airQualityStageComment()[index]

                            if(index > 0 ){
                                OutlinedTextField(
                                    value = "${range}\n${comment}",
                                    onValueChange = {}, // 읽기 전용
                                    readOnly = true,
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    label = { Text(stage.titleKo) },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                        }

                    }
                }
            )

        }


    }



}
package com.unchil.oceanwaterinfo

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureSerialization() {

    install(ContentNegotiation) {
        json()
    }

    install(DefaultHeaders){
        header("Access-Control-Allow-Origin", "*")
    }

    routing{
        get("/") {
            call.respondText("Beautiful World!")
        }

        route("/seoul"){
            get("/sdot_env_info"){
                try {
                    val result = Repository.sDoTEnvInfo()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

        route("/gyonggi"){
            get("/sdot_env_info"){
                try {
                    val result = Repository.sDoTEnvInfoGyonggi()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }



        get("/sdot_env_info"){
            try {
                val result = Repository.sDoTEnvInfoUnion()
                if (result.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(result)
            } catch (ex: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        route("/khnp"){
            get("/wastewater"){
                try {
                    val result = Repository.khnp_WasteWater()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }


            get("/thermalwastewater"){
                try {
                    val result = Repository.khnp_ThermalWasteWater()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/radiorate"){
                try {
                    val result = Repository.khnp_RadioRate()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }




            get("/radioactivewaste"){
                try {
                    val result = Repository.khnp_RadioActiveWaste()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }


            get("/plantstate"){
                try {
                    val result = Repository.khnp_PlantState()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }



        route("/khoa"){

            get("/coastal_flooding_info/geojson_object"){



                val grade = call.parameters["grade"]?.trim() ?: "F"
                val sido = call.parameters["sido"]?.trim() ?: "경기도"
                val type = call.parameters["type"]?.trim() ?: "select"

                LOGGER.info("service call: /coastal_flooding_info/geojson_object:grade:${grade}, sido:${sido}")

                try {
                    val result = Repository.coastalFloodingGeoJsonObject(grade, sido,type)
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    call.respond(result).let {
                        LOGGER.info("response send: /coastal_flooding_info/geojson_object:grade:${grade}, sido:${sido}")
                    }

                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }

            }

            get("/coastal_flooding_info"){

                val defaultRequestedSize = 100
                val maxRequestedSize = 1000

                val page = call.parameters["page"]?.toIntOrNull() ?: 1
                val size = ( call.parameters["size"]?.toIntOrNull() ?: defaultRequestedSize).coerceAtMost(maxRequestedSize)
                val grade = call.parameters["grade"]?.trim() ?: "F"
                val sido = call.parameters["sido"]?.trim() ?: ""

                try {
                    val result = Repository.coastalFloodingGeo(page, size, grade, sido)
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/tidal_current_info"){
                try {
                    val result = Repository.khoaTidalCurrentInfo()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/observationinfo_current"){
                try {
                    val result = Repository.khoaObservationInfoCurrent()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            get("/observationinfo"){
                try {
                    val result = Repository.khoaObservationInfo()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/observatoryinfo"){
                try {
                    val result = Repository.khoaObservatoryInfo()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        route("/nifs") {

            get("/seawaterinfo/{division}"){
                val division = call.parameters["division"]

                if (division == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val result = Repository.seaWaterInfo(division)
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/seawaterinfo/oneDayBoxPlot"){
                try {
                    val result = Repository.seaWaterInfoOneDayBoxPlot("oneDayBoxPlot")
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get ("/stat"){

                try {
                    val result = Repository.seaWaterInfoStatistics()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }

            }

            get ("/observatory"){
                try {
                    val result = Repository.observatoryInfo()
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)

                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }

            }

        }
        route("/mof"){
            get("/swi/{division}"){
                val division = call.parameters["division"]
                if (division == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val result = Repository.swi(division)
                    if (result.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

    }

}
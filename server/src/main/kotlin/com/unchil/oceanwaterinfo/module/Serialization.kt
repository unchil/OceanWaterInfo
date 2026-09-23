package com.unchil.oceanwaterinfo

import com.unchil.oceanwaterinfo.data.ApplicationLogHeader
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
    val cacheExpiryMinute = environment.config.property("cache.expiryMinute").getString().toLong()
    val cacheExpiryDay = environment.config.property("cache.expiryDay").getString().toLong()
    val repository = Repository(cacheExpiryMinute, cacheExpiryDay)

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
                val callUrl = "/seoul/sdot_env_info"
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.sDoTEnvInfo()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

        route("/gyonggi"){
            get("/sdot_env_info"){
                val callUrl = "/gyonggi/sdot_env_info"
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.sDoTEnvInfoGyonggi()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

        get("/sdot_env_info"){
            val callUrl = "/sdot_env_info"
            LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
            try {
                val result = repository.sDoTEnvInfoUnion()
                if (result.isEmpty()) {
                    LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                call.respond(result)
            } catch (ex: IllegalArgumentException) {
                LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        route("/khnp"){
            get("/wastewater"){
                val callUrl = "/khnp/wastewater"
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.khnp_WasteWater()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }


            get("/thermalwastewater"){
                val callUrl = "/khnp/thermalwastewater"
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.khnp_ThermalWasteWater()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/radiorate"){
                val callUrl = "/khnp/radiorate"
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.khnp_RadioRate()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/radioactivewaste"){
                val callUrl = "/khnp/radioactivewaste"
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.khnp_RadioActiveWaste()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }


            get("/plantstate"){
                val callUrl = "/khnp/plantstate"
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.khnp_PlantState()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }



        route("/khoa"){
            get("/coastal_flooding_info/geojson_object"){
                val callUrl = "/khoa/coastal_flooding_info/geojson_object"

                val grade = call.parameters["grade"]?.trim() ?: "F"
                val sido = call.parameters["sido"]?.trim() ?: "경기도"
                val type = call.parameters["type"]?.trim() ?: "select"

                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}?grade=${grade}&sido=${sido}")

                try {
                    val result = repository.coastalFloodingGeoJsonObject(grade, sido,type)
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}: ${callUrl}?grade=${grade}&sido=${sido}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}: ${callUrl}?grade=${grade}&sido=${sido}")
                    call.respond(result)

                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}: ${callUrl}?grade=${grade}&sido=${sido}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }

            }

            get("/coastal_flooding_info"){

                val callUrl = "/khoa/coastal_flooding_info"

                val defaultRequestedSize = 100
                val maxRequestedSize = 1000

                val page = call.parameters["page"]?.toIntOrNull() ?: 1
                val size = ( call.parameters["size"]?.toIntOrNull() ?: defaultRequestedSize).coerceAtMost(maxRequestedSize)
                val grade = call.parameters["grade"]?.trim() ?: "F"
                val sido = call.parameters["sido"]?.trim() ?: ""

                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.coastalFloodingGeo(page, size, grade, sido)
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/tidal_current_info"){
                val callUrl = "/khoa/tidal_current_info"
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                try {
                    val result = repository.khoaTidalCurrentInfo()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/observationinfo_current"){
                val callUrl = "/khoa/observationinfo_current"
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = repository.khoaObservationInfoCurrent()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            get("/observationinfo"){
                val callUrl = "/khoa/observationinfo"
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = repository.khoaObservationInfo()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/observatoryinfo"){
                val callUrl = "/khoa/observatoryinfo"
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = repository.khoaObservatoryInfo()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        route("/nifs") {

            get("/seawaterinfo/{division}"){

                val division = call.parameters["division"]
                val callUrl = "/nifs/seawaterinfo/$division"

                if (division == null) {
                    LOGGER.debug("${ApplicationLogHeader.Respond_BadRequest.name}: ${callUrl}")
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = repository.seaWaterInfo(division)
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/seawaterinfo/oneDayBoxPlot"){
                val callUrl = "/nifs/seawaterinfo/oneDayBoxPlot"
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = repository.seaWaterInfoOneDayBoxPlot("oneDayBoxPlot")
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get ("/stat"){
                val callUrl = "/nifs/seawaterinfo/stat"

                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = repository.seaWaterInfoStatistics()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }

            }

            get ("/observatory"){
                val callUrl = "/nifs/seawaterinfo/observatory"
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = repository.observatoryInfo()
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }

            }

        }
        route("/mof"){
            get("/swi/{division}"){

                val division = call.parameters["division"]
                val callUrl = "/mof/swi/${division}"

                if (division == null) {
                    LOGGER.debug("${ApplicationLogHeader.Respond_BadRequest.name}: ${callUrl}")
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = repository.swi(division)
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}")
                    call.respond(result)
                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

    }

}
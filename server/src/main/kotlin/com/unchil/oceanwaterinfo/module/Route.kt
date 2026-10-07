package com.unchil.oceanwaterinfo.module

import com.unchil.oceanwaterinfo.LOGGER
import com.unchil.oceanwaterinfo.Repository
import com.unchil.oceanwaterinfo.data.ApplicationLogHeader
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.RouteSouthKoreaAirQuality(){
    get("/airQuality"){
        val callUrl = "/airQuality"
        LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
        try {
            val result = Repository.sDoTEnvInfoUnion()
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
    route("/airQuality"){
        get("/seoul"){
            val callUrl = "/airQuality/seoul"
            LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
            try {
                val result = Repository.sDoTEnvInfo()
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
        get("/gyonggi"){
            val callUrl = "/airQuality/gyonggi"
            LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
            try {
                val result = Repository.sDoTEnvInfoGyonggi()
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

fun Route.RouteKHNPStatusBoard(){
    route("/khnp"){
        get("/wastewater"){
            val callUrl = "/khnp/wastewater"
            LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
            try {
                val result = Repository.khnp_WasteWater()
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
                val result = Repository.khnp_ThermalWasteWater()
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
                val result = Repository.khnp_RadioRate()
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
                val result = Repository.khnp_RadioActiveWaste()
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
                val result = Repository.khnp_PlantState()
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

fun Route.RouteKHOAStatusBoard(){
    route("/khoa"){

        route("/coastal_flooding_info"){
            get("/geojson_object"){
                val grade = call.parameters["grade"]?.trim() ?: "F"
                val sido = call.parameters["sido"]?.trim() ?: "경기도"
                val type = call.parameters["type"]?.trim() ?: "select"

                val callUrl = "/khoa/coastal_flooding_info/geojson_object?grade=${grade}&sido=${sido}"

                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")

                try {
                    val result = Repository.coastalFloodingGeoJsonObject(grade, sido,type)
                    if (result.isEmpty()) {
                        LOGGER.debug("${ApplicationLogHeader.Respond_NotFound.name}: ${callUrl}: ${callUrl}")
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    LOGGER.debug("${ApplicationLogHeader.Respond_Data.name}: ${callUrl}: ${callUrl}")
                    call.respond(result)

                } catch (ex: IllegalArgumentException) {
                    LOGGER.error("${ApplicationLogHeader.Respond_Error.name}: ${callUrl}: ${callUrl}[${ex.localizedMessage}]")
                    call.respond(HttpStatusCode.BadRequest)
                }

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
                val result = Repository.coastalFloodingGeo(page, size, grade, sido)
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
                val result = Repository.khoaTidalCurrentInfo()
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
                val result = Repository.khoaObservationInfoCurrent()
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
                val result = Repository.khoaObservationInfo()
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
                val result = Repository.khoaObservatoryInfo()
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

        get("/waveinfo"){
            val callUrl = "/khoa/waveinfo"
            try {
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                val result = Repository.khoaWaveInfo()
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

fun Route.RouteNIFSStatusBoard(){
    route("/nifs") {
        route("/seawaterinfo") {
            get("/{division}"){
                val division = call.parameters["division"]
                val callUrl = "/nifs/seawaterinfo/$division"

                if (division == null) {
                    LOGGER.debug("${ApplicationLogHeader.Respond_BadRequest.name}: ${callUrl}")
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = Repository.seaWaterInfo(division)
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
        get("/oneDayBoxPlot"){
            val callUrl = "/nifs/oneDayBoxPlot"
            try {
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                val result = Repository.seaWaterInfoOneDayBoxPlot("oneDayBoxPlot")
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
            val callUrl = "/nifs/stat"

            try {
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                val result = Repository.seaWaterInfoStatistics()
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
            val callUrl = "/nifs/observatory"
            try {
                LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                val result = Repository.observatoryInfo()
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

fun Route.RouteMOFStatusBoard(){
    route("/mof"){
        route("swi"){
            get("/{division}"){

                val division = call.parameters["division"]
                val callUrl = "/mof/swi/${division}"

                if (division == null) {
                    LOGGER.debug("${ApplicationLogHeader.Respond_BadRequest.name}: ${callUrl}")
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    LOGGER.debug("${ApplicationLogHeader.Service_Call.name}: ${callUrl}")
                    val result = Repository.swi(division)
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
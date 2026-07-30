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

fun Application.configureSerialization(repository: Repository) {

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
                    val result = repository.sDoTEnvInfo()
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
                    val result = repository.sDoTEnvInfoGyonggi()
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
                val result = repository.sDoTEnvInfoUnion()
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
                    val result = repository.khnp_WasteWater()
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
                    val result = repository.khnp_ThermalWasteWater()
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
                    val result = repository.khnp_RadioRate()
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
                    val result = repository.khnp_RadioActiveWaste()
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
                    val result = repository.khnp_PlantState()
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

            get("/coastal_flooding_info"){
                val page = call.parameters["page"]?.toIntOrNull() ?: 1
                val size = call.parameters["size"]?.toIntOrNull() ?: 100

                try {
                    val result = repository.coastalFloodingGeo(page, size)
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
                    val result = repository.khoaTidalCurrentInfo()
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
                    val result = repository.khoaObservationInfoCurrent()
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
                    val result = repository.khoaObservationInfo()
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
                    val result = repository.khoaObservatoryInfo()
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
                    val result = repository.seaWaterInfo(division)
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
                    val result = repository.seaWaterInfoOneDayBoxPlot("oneDayBoxPlot")
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
                    val result = repository.seaWaterInfoStatistics()
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
                    val result = repository.observatoryInfo()
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
                    val result = repository.swi(division)
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
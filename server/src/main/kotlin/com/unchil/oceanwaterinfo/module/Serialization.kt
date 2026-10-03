package com.unchil.oceanwaterinfo

import com.unchil.oceanwaterinfo.module.RouteKHNPStatusBoard
import com.unchil.oceanwaterinfo.module.RouteKHOAStatusBoard
import com.unchil.oceanwaterinfo.module.RouteMOFStatusBoard
import com.unchil.oceanwaterinfo.module.RouteNIFSStatusBoard
import com.unchil.oceanwaterinfo.module.RouteSouthKoreaAirQuality
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing


fun Application.configureSerialization() {

    Repository.init(environment.config)

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
        RouteSouthKoreaAirQuality()
        RouteKHNPStatusBoard()
        RouteKHOAStatusBoard()
        RouteNIFSStatusBoard()
        RouteMOFStatusBoard()
    }

}
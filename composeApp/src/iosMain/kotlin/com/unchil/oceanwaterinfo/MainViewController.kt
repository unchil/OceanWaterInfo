package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position


fun MainViewController() = ComposeUIViewController {
    CompositionLocalProvider( LocalPlatform provides getPlatform() ) {
       // App()
        OceanWaterInfoBarChart()
      //  KoreaMap()
    }
}


@Composable
fun KoreaMap(){
    val cameraState =
        rememberCameraState(
            firstPosition =
                CameraPosition(
                    target = Position(latitude = 37.385847, longitude = 126.934393),
                    zoom = 5.0
                )
        )

    MaplibreMap(
        baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
        cameraState = cameraState,
    )
}

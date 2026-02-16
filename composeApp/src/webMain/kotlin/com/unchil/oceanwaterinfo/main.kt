package com.unchil.oceanwaterinfo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        CompositionLocalProvider( LocalPlatform provides getPlatform() ) {
            OceanWaterInfo()

            //KoreaMap()
        }
    }
}
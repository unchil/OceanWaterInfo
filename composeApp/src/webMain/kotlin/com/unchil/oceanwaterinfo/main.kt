package com.unchil.oceanwaterinfo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {

        MaterialTheme(
            typography = getTypography(),
            colorScheme = getColorScheme(false)
        ) {

            CompositionLocalProvider(LocalPlatform provides getPlatform()) {
                OceanWaterInfo()
            }
        }
    }
}
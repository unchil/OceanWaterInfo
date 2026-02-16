package com.unchil.oceanwaterinfo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController


fun MainViewController() = ComposeUIViewController {

    MaterialTheme(
        colorScheme = getColorScheme(false)
    ) {
        CompositionLocalProvider(LocalPlatform provides getPlatform()) {
         //    App()
            OceanWaterInfoBarChart()
         //   OceanWaterInfoDataGrid()
        }
    }



}

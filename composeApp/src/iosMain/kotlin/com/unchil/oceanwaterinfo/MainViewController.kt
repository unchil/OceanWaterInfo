package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController


fun MainViewController() = ComposeUIViewController {

    MaterialTheme(
        colorScheme = getColorScheme(false)
    ) {
        CompositionLocalProvider(LocalPlatform provides getPlatform()) {
            MainView(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            )
        }
    }



}

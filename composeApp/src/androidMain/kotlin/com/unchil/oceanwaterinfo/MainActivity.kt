package com.unchil.oceanwaterinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme(
                colorScheme = getColorScheme(false)
            ) {
                
                CompositionLocalProvider(LocalPlatform provides getPlatform()) {
                     OceanWaterInfo()

                }

            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.util.MainTab
import com.unchil.oceanwaterinfo.util.MainTab.iframeUrl
import com.unchil.oceanwaterinfo.util.MainTab.subTitle


@Composable
fun EarthOcean(){

    val localUrl = MainTab.TabItem.EARTH_OCEAN.iframeUrl()

    val webController = remember { PlatformWebViewController() }

    val bottomBarHeight = remember{80.dp}

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val height = this.maxHeight

        Column(modifier=Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Column(
                modifier = Modifier.fillMaxWidth().height(height - bottomBarHeight),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                PageTitle(
                    MainTab.TabItem.EARTH_OCEAN.subTitle(),
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 15.dp)
                )

                PlatformWebView(
                    url = localUrl,
                    controller = webController,
                    modifier = Modifier.fillMaxSize(),
                )

            }

            CaptionText(
                "from https://earth.nullschool.net",
                textAlign = TextAlign.Center
            )


        }



    }


}
package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.util.DIV_WEB_MAIN
import com.unchil.oceanwaterinfo.util.MainTab
import com.unchil.oceanwaterinfo.util.MainTab.TabItem
import com.unchil.oceanwaterinfo.util.MainTab.elementID
import com.unchil.oceanwaterinfo.util.MainTab.subTitle

@Composable
fun webMainEarthEnv(){
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val height = this.maxHeight

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            PageTitle(
                TabItem.EARTH_ENVIRONMENTAL.subTitle(),
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 15.dp)
            )

            CaptionText(
                "from https://earth.nullschool.net",
                textAlign = TextAlign.Center
            )


            Box(
                modifier = Modifier.fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        syncHtmlElementPosition(
                            coordinates,
                            density,
                            DIV_WEB_MAIN,
                            MainTab.TabItem.EARTH_ENVIRONMENTAL.elementID()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                //
            }


        }
    }
}
package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

@Composable
actual fun PlatformWebView(
    url: String,
    controller: PlatformWebViewController,
    modifier: Modifier
) {
    val state = rememberWebViewState(url)
    val navigator = rememberWebViewNavigator()

    // KevinnZou 라이브러리의 navigator가 가진 JS 실행 기능을 공통 controller와 매핑
    LaunchedEffect(state.loadingState) {
        controller.loadingState = state.loadingState

        if(state.loadingState is LoadingState.Finished) {
            controller.evaluateJavaScriptImpl = { script ->
                navigator.evaluateJavaScript(script)
            }
        }
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier
    )
}

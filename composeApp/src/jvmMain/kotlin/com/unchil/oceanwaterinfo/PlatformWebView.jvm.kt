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

    LaunchedEffect(state.loadingState) {

        when(state.loadingState){
            LoadingState.Finished -> {
                controller.loadingState = WebViewLoadingState.Finished
                controller.evaluateJavaScriptImpl = { script ->
                    navigator.evaluateJavaScript(script)
                }
            }
            LoadingState.Initializing -> {
                controller.loadingState = WebViewLoadingState.Initializing
            }
            is LoadingState.Loading -> {
                controller.loadingState = WebViewLoadingState.Loading
            }
        }
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier
    )
}

package com.unchil.oceanwaterinfo

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    url: String,
    controller: PlatformWebViewController,
    modifier: Modifier
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
             //   webViewClient = WebViewClient()

                // 공통 컨트롤러 콜백을 Android WebView의 evaluateJavascript와 매핑
                controller.evaluateJavaScriptImpl = { script ->
                    post {
                        evaluateJavascript(script, null)
                    }
                }
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )

}
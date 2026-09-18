package com.unchil.oceanwaterinfo

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    url: String,
    controller: PlatformWebViewController,
    modifier: Modifier
) {

    // Android 에뮬레이터에서 WebView를 통해 Google Maps의 벡터 맵(Vector Map)이나 WebGL 기능을 정상적으로 구동하려면
    // 에뮬레이터의 하드웨어 가속 설정과 Google Maps API 설정 두 가지를 모두 확인해야 합니다.
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // 벡터 맵/WebGL을 위해 하드웨어 가속 레이어 타입 설정
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    // 벡터 맵 성능을 위해 아래 설정도 권장합니다.
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }



                webViewClient =  object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // 로딩 완료를 컨트롤러에 알림
                        controller.loadingState = WebViewLoadingState.Finished
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        controller.loadingState = WebViewLoadingState.Error
                    }

                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        // JS의 console.log, console.error 등이 Android Logcat에 찍힙니다.
                        println("WebView Console: [${consoleMessage?.messageLevel()}] ${consoleMessage?.message()}")
                        return true
                    }

                }

                // 공통 컨트롤러 콜백을 Android WebView의 evaluateJavascript와 매핑
                controller.evaluateJavaScriptImpl = { script ->
                    post {
                        evaluateJavascript(script){ result ->

                        }
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
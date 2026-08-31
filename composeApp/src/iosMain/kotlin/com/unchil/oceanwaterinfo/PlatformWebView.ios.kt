package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIViewController
import platform.WebKit.WKWebView
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    controller: PlatformWebViewController,
    modifier: Modifier
) {

    // 1. Navigation Delegate 구현 (NSObject 상속 필수)
    val navigationDelegate = remember {

        object : platform.darwin.NSObject(), platform.WebKit.WKNavigationDelegateProtocol {

            override fun webView(webView: WKWebView, didFailNavigation: platform.WebKit.WKNavigation?, withError: platform.Foundation.NSError) {
                controller.loadingState = "Error"
            }

            override fun webView(webView: WKWebView, didFinishNavigation: platform.WebKit.WKNavigation?) {
                controller.loadingState = "Finished"
            }
        }
    }

    val wkWebView = remember {
        WKWebView().apply {
            // 2. 델리게이트 연결
            this.navigationDelegate = navigationDelegate
        }
    }

    // 공통 컨트롤러 콜백을 iOS WKWebView의 evaluateJavaScript와 매핑 (메인스레드 보장)
    controller.evaluateJavaScriptImpl = { script ->
        dispatch_async(dispatch_get_main_queue()) {
            wkWebView.evaluateJavaScript(script) { _, error ->
                if (error != null) {
                    println("iOS JS Error: ${error.localizedDescription}")
                }
            }
        }
    }


    UIKitViewController(
        factory = {
            val viewController = UIViewController()
            viewController.view = wkWebView
            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl != null) {
                wkWebView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
            }
            viewController },
        modifier = modifier,
        update = { },
        properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true)
    )


}



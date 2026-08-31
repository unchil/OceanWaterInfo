package com.unchil.oceanwaterinfo



import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier


class PlatformWebViewController {
    // 각 플랫폼(Android, iOS, Jvm)의 실제 웹뷰 객체가 이 콜백을 채워 넣습니다.
    var evaluateJavaScriptImpl: ((String) -> Unit)? = null

    // 외부에서 관찰 가능하도록 상태 추가
    var loadingState by mutableStateOf<Any?>(null)


    // 외부 Compose UI에서 웹뷰의 JS를 실행할 때 호출하는 함수
    fun callJavaScript(functionName: String, args: String = "") {
        val script = "$functionName($args);"
        evaluateJavaScriptImpl?.invoke(script)
    }
}

@Composable
expect fun PlatformWebView(
    url: String,
    controller: PlatformWebViewController,
    modifier: Modifier = Modifier
)
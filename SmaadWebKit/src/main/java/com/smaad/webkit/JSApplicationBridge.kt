package com.smaad.webkit


import android.content.Context
import android.webkit.JavascriptInterface

class JSApplicationBridge(
    private val context: Context
) {

    @JavascriptInterface
    fun onCloseAction(newInterval: String) {
            (context as? WebViewActivity)?.finish()
    }
}

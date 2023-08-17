package com.smaad.webkit

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity

class OpenWebView {
    companion object{
        const val WEB_URL = "WEB_URL"
        const val LIST_DOMAIN = "LIST_DOMAIN"
        fun showWebView(context: Context, url: String, listDomain: ArrayList<String>?){
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(WEB_URL, url)
                if (listDomain != null) {
                    if (listDomain.isNotEmpty())
                        putStringArrayListExtra(LIST_DOMAIN, listDomain)
                }
            }
            startActivity(context, intent, null)
        }
    }
}
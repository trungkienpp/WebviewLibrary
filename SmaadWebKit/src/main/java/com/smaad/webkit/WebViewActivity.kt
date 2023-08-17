package com.smaad.webkit

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.webkit.HttpAuthHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.smaad.webkit.databinding.ActivityWebViewBinding
import java.net.URI


class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private var domainURL = ""
    private var domainList =  ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        val webURL = intent.getStringExtra(OpenWebView.WEB_URL)
        if (intent.hasExtra(OpenWebView.LIST_DOMAIN)) {
            domainList = intent.getStringArrayListExtra(OpenWebView.LIST_DOMAIN) as ArrayList<String>
        }
        webURL?.let {
            domainURL = if (it.startsWith("http://") || it.startsWith("https://")) {
                val uri = URI(it)
                uri.host
            } else {
                val uri = URI("http://$it")
                uri.host
            }
            initWebView(it)
        }
        setContentView(binding.root)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(url: String) {
        val webView = binding.webView
        webView.setPadding(0, 0, 0, 0)
        webView.setInitialScale(1)
        webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            domStorageEnabled = true
        }

        val jsBridge = JSApplicationBridge(this@WebViewActivity)
        webView.addJavascriptInterface(jsBridge, "MyButtonNative")
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler?,
                host: String?,
                realm: String?
            ) {
                val builder = AlertDialog.Builder(this@WebViewActivity)
                builder.setTitle("Authentication Required")
                builder.setMessage("Enter your login credentials")

                val inputLayout = LinearLayout(this@WebViewActivity)
                inputLayout.orientation = LinearLayout.VERTICAL

                val usernameInput = EditText(this@WebViewActivity)
                usernameInput.hint = "Username"
                inputLayout.addView(usernameInput)

                val passwordInput = EditText(this@WebViewActivity)
                passwordInput.hint = "Password"
                passwordInput.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                inputLayout.addView(passwordInput)

                builder.setView(inputLayout)

                builder.setPositiveButton("OK") { dialog, which ->
                    val username = usernameInput.text.toString()
                    val password = passwordInput.text.toString()
                    handler?.proceed(username, password)
                }

                builder.setNegativeButton("Cancel") { dialog, which ->
                    handler?.cancel()
                }

                builder.show()
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (domainURL.isNotEmpty()) {
                    return if (!checkContainDomain(url)) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(browserIntent)
                        true
                    } else {
                        view.loadUrl(url)
                        false
                    }
                }
                return false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Log.e("Webview Error: ", error.toString())
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                Log.e("Webview Error: ", errorResponse.toString())
            }

        }
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(url)
    }

    private fun checkContainDomain(url: String): Boolean {
        if (url.contains(domainURL)) {
            return true
        } else {
            if (domainList.isNotEmpty()) {
                for (item in domainList) {
                    if (url.contains(item)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
package com.github.abaker.googlevoice.login

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.github.abaker.googlevoice.GoogleVoiceClient
import timber.log.Timber

class MyWebViewClient(
    val client: GoogleVoiceClient = GoogleVoiceClient(),
    val onLogin: () -> Unit,
) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        Timber.v("onPageStarted(<view>, $url, <favicon>)")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        Timber.v("onPageFinished(<view>, $url): ${CookieManager.getInstance().getCookie(url)}")
        if (client.isLoggedIn) {
            onLogin()
        }
    }
}
package com.github.abaker.googlevoice

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.abaker.googlevoice.list.MessageList
import com.github.abaker.googlevoice.login.MyWebViewClient
import com.github.abaker.googlevoice.ui.theme.GoogleVoiceTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        val client = GoogleVoiceClient()
        val startDestination = if (client.isLoggedIn) {
            "list"
        } else {
            "login"
        }
        setContent {
            val navController = rememberNavController()
            GoogleVoiceTheme {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    webViewClient = MyWebViewClient {
                                        navController.navigate("list")
                                    }
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.setSupportZoom(false)
                                    settings.userAgentString = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/109.0"
                                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                                }
                            },
                            update = { webView ->
                                webView.loadUrl("https://voice.google.com/signup")
                            }
                        )
                    }
                    composable("list") {
                        MessageList()
                    }
                }
            }
        }
    }
}

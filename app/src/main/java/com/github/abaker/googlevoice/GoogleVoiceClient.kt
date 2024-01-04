package com.github.abaker.googlevoice

import android.webkit.CookieManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import timber.log.Timber
import java.security.MessageDigest

class GoogleVoiceClient {
    private val http = HttpClient(OkHttp) {
        install(ContentEncoding) {
            deflate(1.0f)
            gzip(0.9f)
        }
    }
    val isLoggedIn: Boolean
        get() = cookies.sapisid?.isNotBlank() == true

    suspend fun getThreads(): String? {
        val response =
            http.post("$API_URL/api2thread/list?alt=protojson&key=$API_KEY") {
//            http.post("$API_URL/api2thread/list?alt=json&key=$API_KEY") {
                setBody("[2,20,15,null,null,[null,1,1]]")
                contentType(ContentType("application", "json+protobuf"))
                headers.apply {
                    append("Accept", "*/*")
                    append("Accept-Language", "en-US,en;q=0.5")
                    append(
                        "X-ClientDetails",
                        "appVersion=5.0%20(X11%3B%20Ubuntu)&platform=Linux%20x86_64&userAgent=Mozilla%2F5.0%20(X11%3B%20Ubuntu%3B%20Linux%20x86_64%3B%20rv%3A109.0)%20Gecko%2F20100101%20Firefox%2F109.0"
                    )
                    append("Authorization", cookies.sapisid ?: return null)
                    append("X-Goog-AuthUser", "0")
                    append("X-Requested-With", "XMLHttpRequest")
                    append("X-JavaScript-User-Agent", "google-api-javascript-client/1.1.0")
                    append("X-Client-Version", "594856634")
                    append("X-Origin", "https://voice.google.com")
                    append("X-Referer", "https://voice.google.com")
                    append("X-Goog-Encode-Response-If-Executable", "base64")
                    append("Origin", "https://clients6.google.com")
                    append("Connection", "keep-alive")
                    append("Sec-Fetch-Dest", "empty")
                    append("Sec-Fetch-Mode", "cors")
                    append("Sec-Fetch-Site", "same-origin")
                    append("DNT", "1")
                    append("Sec-GPC", "1")
                    append("TE", "trailers")
                    append("Cookie", cookie ?: return null)

                    append("Referer", "https://clients6.google.com/static/proxy.html?usegapi=1")

                    append(
                        "User-Agent",
                        "Mozilla/5.0 (X11; Linux x86_64; rv:68.0) Gecko/20100101 Firefox/68.0"
                    )
                    append("Pragma", "no-cache")
                    append("Cache-Control", "no-cache")
                }
            }
        return response.bodyAsText()
    }

    private val cookie: String?
        get() = CookieManager
            .getInstance()
            .getCookie("https://voice.google.com")
            ?.takeIf { it.isNotBlank() }

    private val cookies: List<Pair<String?, String?>>
        get() = cookie
            ?.split("; ")
            ?.map { it.split("=", limit = 2) }
            ?.map { Pair(it.getOrNull(0), it.getOrNull(1)) }
            ?: emptyList()

    private val List<Pair<String?, String?>>.sapisid: String?
        get() =
            firstOrNull { (key, _) -> key == "SAPISID" }
                ?.let { (key, value) ->
                    val now = System.currentTimeMillis() / 1000
                    val hash = value?.sha1Hash(now) ?: return@let null
                    "${key}HASH ${now}_$hash"
                }
                ?.also { Timber.v("hash: $it") }

    companion object {
        const val API_URL = "https://clients6.google.com/voice/v1/voiceclient"
        const val API_KEY = "AIzaSyDTYc1N4xiODyrQYK0Kl6g_y279LjYkrBg"

        private fun String.sha1Hash(now: Long): String =
            MessageDigest
                .getInstance("SHA-1")
                .digest("$now $this https://voice.google.com".encodeToByteArray())
                .joinToString("") { "%02x".format(it) }
    }
}

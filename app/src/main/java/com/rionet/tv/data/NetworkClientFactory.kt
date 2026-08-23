package com.rionet.tv.data

import com.rionet.tv.model.ProxyConfig
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object NetworkClientFactory {
    fun create(proxyConfig: ProxyConfig, readTimeoutSeconds: Long = 45): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        if (proxyConfig.isUsable) {
            builder.proxy(
                Proxy(
                    Proxy.Type.HTTP,
                    InetSocketAddress.createUnresolved(proxyConfig.host.trim(), proxyConfig.port)
                )
            )
            if (proxyConfig.username.isNotBlank()) {
                builder.proxyAuthenticator { _, response ->
                    if (response.request.header("Proxy-Authorization") != null) return@proxyAuthenticator null
                    response.request.newBuilder()
                        .header("Proxy-Authorization", Credentials.basic(proxyConfig.username, proxyConfig.password))
                        .build()
                }
            }
        }
        return builder.build()
    }
}

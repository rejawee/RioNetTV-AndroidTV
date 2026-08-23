package com.rionet.tv.data

import com.rionet.tv.model.LiveFootballMatch
import com.rionet.tv.model.ProxyConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

class SportmonksRepository {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun inPlay(token: String, proxy: ProxyConfig = ProxyConfig()): List<LiveFootballMatch> = withContext(Dispatchers.IO) {
        if (token.isBlank()) return@withContext emptyList()
        val client = NetworkClientFactory.create(proxy)
        val url = "https://api.sportmonks.com/v3/football/livescores/inplay".toHttpUrl().newBuilder()
            .addQueryParameter("api_token", token)
            .addQueryParameter("include", "participants;scores;tvstations")
            .build()
        val text = client.newCall(Request.Builder().url(url).build()).execute().use {
            if (!it.isSuccessful) return@withContext emptyList()
            it.body?.string().orEmpty()
        }
        val data = json.parseToJsonElement(text).jsonObject["data"]?.jsonArray ?: return@withContext emptyList()
        data.mapNotNull { e ->
            val o = e.jsonObject
            val id = o["id"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            LiveFootballMatch(id, o["name"]?.jsonPrimitive?.contentOrNull ?: "مباراة", o["starting_at"]?.jsonPrimitive?.contentOrNull ?: "")
        }
    }
}

package com.rionet.tv.data

import com.rionet.tv.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

class XtreamRepository {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun load(config: XtreamConfig, proxy: ProxyConfig = ProxyConfig()): Catalog = withContext(Dispatchers.IO) {
        val base = config.server.trim().trimEnd('/')
        require(base.isNotBlank() && config.username.isNotBlank() && config.password.isNotBlank()) { "أدخل بيانات Xtream كاملة" }
        val client = NetworkClientFactory.create(proxy)
        val auth = get(client, base, config, null)
        val authObject = auth as? JsonObject ?: error("استجابة Xtream الرئيسية غير صالحة")
        val user = authObject["user_info"] as? JsonObject
        require(user?.get("auth")?.stringValue != "0") { "بيانات الاشتراك غير صحيحة أو منتهية" }

        val liveCats = categoryMap(array(client, base, config, "get_live_categories"))
        val vodCats = categoryMap(array(client, base, config, "get_vod_categories"))
        val seriesCats = categoryMap(array(client, base, config, "get_series_categories"))
        val liveRows = array(client, base, config, "get_live_streams")
        val vodRows = array(client, base, config, "get_vod_streams")
        val seriesRows = array(client, base, config, "get_series")

        val live = liveRows.mapIndexedNotNull { i, e ->
            val o = e as? JsonObject ?: return@mapIndexedNotNull null
            val sid = o.str("stream_id") ?: return@mapIndexedNotNull null
            val cat = liveCats[o.str("category_id")] ?: "غير مصنف"
            MediaEntry(
                "live-$sid", o.str("name") ?: "قناة $sid", cat, cat, o.artwork(),
                listOf(
                    "$base/live/${path(config.username)}/${path(config.password)}/$sid.m3u8",
                    "$base/live/${path(config.username)}/${path(config.password)}/$sid.ts"
                ),
                MediaKind.LIVE,
                serverUpdatedAt = o.long("added"),
                serverOrder = i
            )
        }

        val moviesRaw = vodRows.mapIndexedNotNull { i, e ->
            val o = e as? JsonObject ?: return@mapIndexedNotNull null
            val sid = o.str("stream_id") ?: return@mapIndexedNotNull null
            val type = (o.str("stream_type") ?: o.str("type") ?: "").lowercase()
            if ("live" in type) return@mapIndexedNotNull null
            val cat = vodCats[o.str("category_id")] ?: "غير مصنف"
            val ext = o.str("container_extension") ?: "mp4"
            MediaEntry(
                "movie-$sid", o.str("name") ?: "فيلم $sid", cat, cat, o.artwork(),
                listOf("$base/movie/${path(config.username)}/${path(config.password)}/$sid.$ext"),
                MediaKind.MOVIE,
                o.str("year") ?: "",
                o.str("rating") ?: "",
                o.long("added"),
                i
            )
        }

        val seriesRaw = seriesRows.mapIndexedNotNull { i, e ->
            val o = e as? JsonObject ?: return@mapIndexedNotNull null
            val sid = o.str("series_id") ?: return@mapIndexedNotNull null
            val cat = seriesCats[o.str("category_id")] ?: "غير مصنف"
            SeriesEntry(
                sid,
                o.str("name") ?: "مسلسل $sid",
                cat,
                o.artwork(),
                o.str("year") ?: "",
                o.str("rating") ?: "",
                o.long("last_modified"),
                i
            )
        }

        sanitize(Catalog(live, moviesRaw, seriesRaw))
    }

    suspend fun episodes(config: XtreamConfig, seriesId: String, proxy: ProxyConfig = ProxyConfig()): List<MediaEntry> = withContext(Dispatchers.IO) {
        val base = config.server.trim().trimEnd('/')
        val client = NetworkClientFactory.create(proxy)
        val root = get(client, base, config, "get_series_info", mapOf("series_id" to seriesId)) as? JsonObject
            ?: return@withContext emptyList()
        val episodesElement = root["episodes"] ?: return@withContext emptyList()

        val grouped: List<Pair<String, JsonArray>> = when (episodesElement) {
            is JsonObject -> episodesElement.entries.mapNotNull { (season, value) ->
                value.toJsonArrayOrNull()?.let { season to it }
            }.sortedBy { it.first.toIntOrNull() ?: Int.MAX_VALUE }
            is JsonArray -> listOf("1" to episodesElement)
            else -> emptyList()
        }

        grouped.flatMap { (season, values) ->
            values.mapIndexedNotNull { i, e ->
                val o = e as? JsonObject ?: return@mapIndexedNotNull null
                val id = o.str("id") ?: o.str("stream_id") ?: return@mapIndexedNotNull null
                val ext = o.str("container_extension") ?: "mp4"
                val ep = o.str("episode_num") ?: "${i + 1}"
                val info = o["info"] as? JsonObject
                MediaEntry(
                    "episode-$id",
                    o.str("title") ?: o.str("name") ?: "الحلقة $ep",
                    "الموسم $season • الحلقة $ep",
                    posterUrl = info?.artwork() ?: o.artwork(),
                    streamUrls = listOf("$base/series/${path(config.username)}/${path(config.password)}/$id.$ext"),
                    kind = MediaKind.EPISODE,
                    serverOrder = i
                )
            }
        }
    }

    private fun sanitize(c: Catalog): Catalog {
        val liveKeys = c.live.map { key(it.title) }.filter { it.length > 3 }.toSet()
        val liveCats = c.live.map { key(it.category) }.filter { it.isNotBlank() }.toSet()
        fun suspicious(title: String, category: String, year: String, rating: String): Boolean {
            val k = key(title)
            val family = k.replace(Regex("\\b(uhd|fhd|hd|4k|8k|sd|hevc|h265|h264)\\b"), "").trim()
            val titleMatch = liveKeys.any { it == k || it.replace(Regex("\\b(uhd|fhd|hd|4k|8k|sd)\\b"), "").trim() == family }
            val categoryOverlap = key(category) in liveCats
            val noVodMetadata = year.isBlank() && rating.isBlank()
            return titleMatch || (categoryOverlap && noVodMetadata && Regex("\\b(tv|sport|sports|news|cinema|channel|bein|osn|mbc|ad sport|ksa sport)\\b", RegexOption.IGNORE_CASE).containsMatchIn(k))
        }
        val movies = c.movies.filterNot { suspicious(it.title, it.category, it.year, it.rating) }
        val series = c.series.filterNot { suspicious(it.title, it.category, it.year, it.rating) }
        return c.copy(movies = movies, series = series)
    }

    private fun categoryMap(a: JsonArray) = a.mapNotNull { e ->
        val o = e as? JsonObject ?: return@mapNotNull null
        val id = o.str("category_id") ?: return@mapNotNull null
        id to (o.str("category_name") ?: o.str("name") ?: "غير مصنف")
    }.toMap()

    private suspend fun array(
        client: okhttp3.OkHttpClient,
        base: String,
        c: XtreamConfig,
        action: String
    ): JsonArray {
        val element = get(client, base, c, action)
        return element.toJsonArrayOrNull() ?: JsonArray(emptyList())
    }

    private fun JsonElement.toJsonArrayOrNull(): JsonArray? = when (this) {
        is JsonArray -> this
        is JsonObject -> {
            val known = listOf("data", "results", "items", "categories", "streams", "series")
                .firstNotNullOfOrNull { key -> this[key] as? JsonArray }
            known ?: values.takeIf { it.isNotEmpty() && it.all { value -> value is JsonObject } }
                ?.let { JsonArray(it.toList()) }
        }
        else -> null
    }

    private suspend fun get(
        client: okhttp3.OkHttpClient,
        base: String,
        c: XtreamConfig,
        action: String?,
        extra: Map<String, String> = emptyMap()
    ): JsonElement {
        val b = "$base/player_api.php".toHttpUrl().newBuilder()
            .addQueryParameter("username", c.username)
            .addQueryParameter("password", c.password)
        action?.let { b.addQueryParameter("action", it) }
        extra.forEach { (k, v) -> b.addQueryParameter(k, v) }
        val r = client.newCall(Request.Builder().url(b.build()).build()).execute()
        r.use {
            require(it.isSuccessful) { "HTTP ${it.code}" }
            val raw = it.body?.string().orEmpty().trim()
            require(raw.isNotBlank()) { "استجابة فارغة من Xtream${action?.let { a -> " • $a" } ?: ""}" }
            return runCatching { json.parseToJsonElement(raw) }
                .getOrElse { cause -> error("استجابة JSON غير صالحة من Xtream${action?.let { a -> " • $a" } ?: ""}: ${cause.message}") }
        }
    }

    private fun path(v: String) = java.net.URLEncoder.encode(v, "UTF-8").replace("+", "%20")
    private fun key(v: String) = v.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
}

private fun JsonObject.str(k: String): String? = this[k]?.stringValue
private fun JsonObject.long(k: String): Long = str(k)?.toLongOrNull() ?: 0L
private val JsonElement.stringValue: String? get() = when (this) { is JsonPrimitive -> contentOrNull; else -> null }
private fun JsonObject.artwork(): String = listOf("stream_icon", "cover", "cover_big", "movie_image", "movie_image_big").firstNotNullOfOrNull { str(it)?.takeIf(String::isNotBlank) } ?: ""

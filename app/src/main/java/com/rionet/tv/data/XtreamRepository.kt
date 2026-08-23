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
        val user = auth.jsonObject["user_info"]?.jsonObject
        require(user?.get("auth")?.stringValue != "0") { "بيانات الاشتراك غير صحيحة أو منتهية" }

        val liveCats = categoryMap(array(client, base, config, "get_live_categories"))
        val vodCats = categoryMap(array(client, base, config, "get_vod_categories"))
        val seriesCats = categoryMap(array(client, base, config, "get_series_categories"))
        val liveRows = array(client, base, config, "get_live_streams")
        val vodRows = array(client, base, config, "get_vod_streams")
        val seriesRows = array(client, base, config, "get_series")

        val live = liveRows.mapIndexedNotNull { i, e ->
            val o = e.jsonObject; val sid = o.str("stream_id") ?: return@mapIndexedNotNull null
            val cat = liveCats[o.str("category_id")] ?: "غير مصنف"
            MediaEntry("live-$sid", o.str("name") ?: "قناة $sid", cat, cat, o.artwork(),
                listOf("$base/live/${path(config.username)}/${path(config.password)}/$sid.m3u8", "$base/live/${path(config.username)}/${path(config.password)}/$sid.ts"), MediaKind.LIVE, serverUpdatedAt=o.long("added"), serverOrder=i)
        }
        val moviesRaw = vodRows.mapIndexedNotNull { i, e ->
            val o=e.jsonObject; val sid=o.str("stream_id") ?: return@mapIndexedNotNull null
            val type=(o.str("stream_type") ?: o.str("type") ?: "").lowercase(); if ("live" in type) return@mapIndexedNotNull null
            val cat=vodCats[o.str("category_id")] ?: "غير مصنف"; val ext=o.str("container_extension") ?: "mp4"
            MediaEntry("movie-$sid", o.str("name") ?: "فيلم $sid", cat, cat, o.artwork(), listOf("$base/movie/${path(config.username)}/${path(config.password)}/$sid.$ext"), MediaKind.MOVIE, o.str("year") ?: "", o.str("rating") ?: "", o.long("added"), i)
        }
        val seriesRaw = seriesRows.mapIndexedNotNull { i,e ->
            val o=e.jsonObject; val sid=o.str("series_id") ?: return@mapIndexedNotNull null; val cat=seriesCats[o.str("category_id")] ?: "غير مصنف"
            SeriesEntry(sid, o.str("name") ?: "مسلسل $sid", cat, o.artwork(), o.str("year") ?: "", o.str("rating") ?: "", o.long("last_modified"), i)
        }
        sanitize(Catalog(live, moviesRaw, seriesRaw))
    }

    suspend fun episodes(config: XtreamConfig, seriesId: String, proxy: ProxyConfig = ProxyConfig()): List<MediaEntry> = withContext(Dispatchers.IO) {
        val base=config.server.trim().trimEnd('/'); val client=NetworkClientFactory.create(proxy); val root=get(client,base,config,"get_series_info", mapOf("series_id" to seriesId)).jsonObject
        val eps=root["episodes"]?.jsonObject ?: return@withContext emptyList()
        eps.entries.sortedBy { it.key.toIntOrNull() ?: Int.MAX_VALUE }.flatMap { (season,value) ->
            value.jsonArray.mapIndexedNotNull { i,e ->
                val o=e.jsonObject; val id=o.str("id") ?: return@mapIndexedNotNull null; val ext=o.str("container_extension") ?: "mp4"; val ep=o.str("episode_num") ?: "${i+1}"
                MediaEntry("episode-$id", o.str("title") ?: "الحلقة $ep", "الموسم $season • الحلقة $ep", posterUrl=o["info"]?.jsonObject?.artwork() ?: "", streamUrls=listOf("$base/series/${path(config.username)}/${path(config.password)}/$id.$ext"), kind=MediaKind.EPISODE, serverOrder=i)
            }
        }
    }

    private fun sanitize(c: Catalog): Catalog {
        val liveKeys=c.live.map { key(it.title) }.filter { it.length>3 }.toSet()
        val liveCats=c.live.map { key(it.category) }.filter { it.isNotBlank() }.toSet()
        fun suspicious(title:String, category:String, year:String, rating:String):Boolean {
            val k=key(title); val family=k.replace(Regex("\\b(uhd|fhd|hd|4k|8k|sd|hevc|h265|h264)\\b"),"").trim()
            val titleMatch=liveKeys.any { it==k || it.replace(Regex("\\b(uhd|fhd|hd|4k|8k|sd)\\b"),"").trim()==family }
            val categoryOverlap=key(category) in liveCats
            val noVodMetadata=year.isBlank() && rating.isBlank()
            return titleMatch || (categoryOverlap && noVodMetadata && Regex("\\b(tv|sport|sports|news|cinema|channel|bein|osn|mbc|ad sport|ksa sport)\\b", RegexOption.IGNORE_CASE).containsMatchIn(k))
        }
        val movies=c.movies.filterNot { suspicious(it.title,it.category,it.year,it.rating) }
        val series=c.series.filterNot { suspicious(it.title,it.category,it.year,it.rating) }
        return c.copy(movies=movies, series=series)
    }

    private fun categoryMap(a:JsonArray)=a.mapNotNull { e -> val o=e.jsonObject; val id=o.str("category_id") ?: return@mapNotNull null; id to (o.str("category_name") ?: "غير مصنف") }.toMap()
    private suspend fun array(client:okhttp3.OkHttpClient,base:String,c:XtreamConfig,action:String)=get(client,base,c,action).jsonArray
    private suspend fun get(client:okhttp3.OkHttpClient,base:String,c:XtreamConfig,action:String?,extra:Map<String,String> = emptyMap()):JsonElement {
        val b="$base/player_api.php".toHttpUrl().newBuilder().addQueryParameter("username",c.username).addQueryParameter("password",c.password)
        action?.let { b.addQueryParameter("action",it) }; extra.forEach { (k,v)->b.addQueryParameter(k,v) }
        val r=client.newCall(Request.Builder().url(b.build()).build()).execute(); r.use { require(it.isSuccessful){"HTTP ${it.code}"}; return json.parseToJsonElement(it.body?.string().orEmpty()) }
    }
    private fun path(v:String)=java.net.URLEncoder.encode(v,"UTF-8").replace("+","%20")
    private fun key(v:String)=v.lowercase().replace(Regex("[^\\p{L}\\p{N}]+")," ").trim()
}

private fun JsonObject.str(k:String):String? = this[k]?.stringValue
private fun JsonObject.long(k:String):Long = str(k)?.toLongOrNull() ?: 0L
private val JsonElement.stringValue:String? get() = when(this){ is JsonPrimitive -> contentOrNull; else -> null }
private fun JsonObject.artwork():String = listOf("stream_icon","cover","cover_big","movie_image").firstNotNullOfOrNull { str(it)?.takeIf(String::isNotBlank) } ?: ""

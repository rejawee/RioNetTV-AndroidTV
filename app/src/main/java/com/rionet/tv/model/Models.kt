package com.rionet.tv.model

enum class AppSection(val title: String) { HOME("الرئيسية"), LIVE("البث المباشر"), MOVIES("الأفلام"), SERIES("المسلسلات"), MATCHES("المباريات"), FAVORITES("المفضلة") }
enum class MediaKind { LIVE, MOVIE, EPISODE }
enum class SourceMode { XTREAM, M3U }

data class MediaEntry(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val category: String = "",
    val posterUrl: String = "",
    val streamUrls: List<String> = emptyList(),
    val kind: MediaKind = MediaKind.LIVE,
    val year: String = "",
    val rating: String = "",
    val serverUpdatedAt: Long = 0,
    val serverOrder: Int = 0
)

data class SeriesEntry(
    val id: String,
    val title: String,
    val category: String = "",
    val posterUrl: String = "",
    val year: String = "",
    val rating: String = "",
    val serverUpdatedAt: Long = 0,
    val serverOrder: Int = 0,
    val episodes: List<MediaEntry> = emptyList()
)

data class Catalog(val live: List<MediaEntry> = emptyList(), val movies: List<MediaEntry> = emptyList(), val series: List<SeriesEntry> = emptyList(), val warnings: List<String> = emptyList())
data class XtreamConfig(val server: String = "", val username: String = "", val password: String = "")
data class IPTVConfig(val mode: SourceMode = SourceMode.XTREAM, val xtream: XtreamConfig = XtreamConfig(), val m3uUrl: String = "")

/**
 * Optional app-level HTTP proxy. When enabled it is used by catalog/API calls and Media3 playback.
 * host must be a hostname/IP only (no scheme); port is normally 8080/3128/8888 depending on provider.
 */
data class ProxyConfig(
    val enabled: Boolean = false,
    val host: String = "",
    val port: Int = 8080,
    val username: String = "",
    val password: String = ""
) {
    val isUsable: Boolean get() = enabled && host.isNotBlank() && port in 1..65535
    val displayName: String get() = if (isUsable) "$host:$port" else "مباشر"
}

data class LiveFootballEvent(val id: Int, val minute: Int = 0, val extraMinute: Int = 0, val teamId: Int = 0, val playerName: String = "", val label: String = "حدث", val info: String = "")
data class LiveFootballMatch(val id: Int, val name: String, val startingAt: String = "", val state: String = "LIVE", val homeName: String = "Home", val awayName: String = "Away", val homeScore: Int = 0, val awayScore: Int = 0, val events: List<LiveFootballEvent> = emptyList(), val tvStations: List<String> = emptyList())

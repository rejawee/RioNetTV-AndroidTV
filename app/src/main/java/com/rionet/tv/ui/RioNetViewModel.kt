package com.rionet.tv.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rionet.tv.data.*
import com.rionet.tv.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request


data class UiState(
    val section: AppSection = AppSection.HOME,
    val config: IPTVConfig = IPTVConfig(),
    val proxy: ProxyConfig = ProxyConfig(),
    val catalog: Catalog = Catalog(),
    val search: String = "",
    val category: String = "الكل",
    val selected: MediaEntry? = null,
    val selectedSeries: SeriesEntry? = null,
    val episodes: List<MediaEntry> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val sportmonksToken: String = "",
    val matches: List<LiveFootballMatch> = emptyList(),
    val loading: Boolean = false,
    val message: String = "",
    val error: String = "",
    val showSettings: Boolean = false,
    val testingProxy: Boolean = false
)

class RioNetViewModel(app: Application) : AndroidViewModel(app) {
    private val xtream = XtreamRepository()
    private val m3u = M3URepository()
    private val settings = SettingsStore(app)
    private val sport = SportmonksRepository()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val stored = settings.load()
            _state.value = _state.value.copy(
                config = stored.config,
                sportmonksToken = stored.sportmonksToken,
                favorites = stored.favorites,
                proxy = stored.proxy
            )
            if (stored.config.xtream.server.isNotBlank() || stored.config.m3uUrl.isNotBlank()) loadCatalog()
        }
    }

    fun setSection(s: AppSection) { _state.value = _state.value.copy(section = s, category = "الكل", search = "") }
    fun search(v: String) { _state.value = _state.value.copy(search = v) }
    fun category(v: String) { _state.value = _state.value.copy(category = v) }
    fun settings(show: Boolean) { _state.value = _state.value.copy(showSettings = show) }
    fun updateConfig(c: IPTVConfig, token: String, proxy: ProxyConfig = state.value.proxy) {
        _state.value = _state.value.copy(config = c, sportmonksToken = token, proxy = proxy)
    }

    fun saveAndLoad() {
        viewModelScope.launch {
            val s = state.value
            settings.save(s.config, s.sportmonksToken, s.favorites, s.proxy)
            settings(false)
            loadCatalog()
        }
    }

    fun testProxy(proxy: ProxyConfig) {
        if (!proxy.enabled) {
            _state.value = _state.value.copy(message = "البروكسي غير مفعّل", error = "")
            return
        }
        if (!proxy.isUsable) {
            _state.value = _state.value.copy(error = "أدخل عنوان البروكسي والمنفذ بشكل صحيح", message = "")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(testingProxy = true, error = "", message = "جاري اختبار البروكسي…")
            val target = state.value.config.xtream.server.takeIf { it.startsWith("http") }
                ?: state.value.config.m3uUrl.takeIf { it.startsWith("http") }
            if (target == null) {
                _state.value = _state.value.copy(testingProxy = false, error = "أدخل سيرفر Xtream أو رابط M3U أولًا لاختبار المسار", message = "")
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val client = NetworkClientFactory.create(proxy, readTimeoutSeconds = 15)
                    client.newCall(Request.Builder().url(target).get().build()).execute().use { response ->
                        "اتصال البروكسي ناجح • HTTP ${response.code}"
                    }
                }
            }
            result.onSuccess { _state.value = _state.value.copy(testingProxy = false, message = it, error = "") }
                .onFailure { _state.value = _state.value.copy(testingProxy = false, error = "فشل البروكسي: ${it.message ?: "تعذر الاتصال"}", message = "") }
        }
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = "")
            val s = state.value
            runCatching {
                if (s.config.mode == SourceMode.XTREAM) xtream.load(s.config.xtream, s.proxy)
                else m3u.load(s.config.m3uUrl, s.proxy)
            }.onSuccess { c ->
                _state.value = _state.value.copy(
                    catalog = c,
                    loading = false,
                    message = "تم تحميل ${c.live.size} قناة • ${c.movies.size} فيلم • ${c.series.size} مسلسل${if (s.proxy.isUsable) " • عبر البروكسي" else ""}",
                    selected = c.live.firstOrNull()
                )
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = it.message ?: "خطأ في التحميل")
            }
        }
    }

    fun select(item: MediaEntry) { _state.value = _state.value.copy(selected = item) }

    fun openSeries(s: SeriesEntry) {
        viewModelScope.launch {
            _state.value = _state.value.copy(selectedSeries = s, loading = true)
            val eps = if (state.value.config.mode == SourceMode.XTREAM)
                runCatching { xtream.episodes(state.value.config.xtream, s.id, state.value.proxy) }.getOrDefault(emptyList())
            else s.episodes
            _state.value = _state.value.copy(episodes = eps, loading = false)
        }
    }

    fun closeSeries() { _state.value = _state.value.copy(selectedSeries = null, episodes = emptyList()) }

    fun toggleFavorite(id: String) {
        val f = state.value.favorites.toMutableSet()
        if (!f.add(id)) f.remove(id)
        _state.value = _state.value.copy(favorites = f)
        viewModelScope.launch {
            val s = state.value
            settings.save(s.config, s.sportmonksToken, f, s.proxy)
        }
    }

    fun refreshMatches() {
        viewModelScope.launch {
            val m = sport.inPlay(state.value.sportmonksToken, state.value.proxy)
            _state.value = _state.value.copy(matches = m)
        }
    }

    fun categories(): List<String> {
        val s = state.value
        val v = when (s.section) {
            AppSection.LIVE -> s.catalog.live.map { it.category }
            AppSection.MOVIES -> s.catalog.movies.map { it.category }
            AppSection.SERIES -> s.catalog.series.map { it.category }
            else -> emptyList()
        }
        return listOf("الكل") + v.filter { it.isNotBlank() }.distinct().sorted()
    }

    fun media(): List<MediaEntry> {
        val s = state.value
        val src = when (s.section) {
            AppSection.LIVE -> s.catalog.live
            AppSection.MOVIES -> s.catalog.movies
            else -> emptyList()
        }
        return src.filter { (s.category == "الكل" || it.category == s.category) && (s.search.isBlank() || it.title.contains(s.search, true) || it.category.contains(s.search, true)) }
    }

    fun series(): List<SeriesEntry> {
        val s = state.value
        return s.catalog.series.filter { (s.category == "الكل" || it.category == s.category) && (s.search.isBlank() || it.title.contains(s.search, true) || it.category.contains(s.search, true)) }
    }
}

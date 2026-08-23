package com.rionet.tv.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rionet.tv.model.*
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("rionet_settings")

data class StoredSettings(
    val config: IPTVConfig,
    val sportmonksToken: String,
    val favorites: Set<String>,
    val proxy: ProxyConfig
)

class SettingsStore(private val context: Context) {
    private object K {
        val mode = stringPreferencesKey("mode")
        val server = stringPreferencesKey("server")
        val user = stringPreferencesKey("user")
        val pass = stringPreferencesKey("pass")
        val m3u = stringPreferencesKey("m3u")
        val sport = stringPreferencesKey("sportmonks")
        val fav = stringSetPreferencesKey("favorites")
        val proxyEnabled = booleanPreferencesKey("proxy_enabled")
        val proxyHost = stringPreferencesKey("proxy_host")
        val proxyPort = intPreferencesKey("proxy_port")
        val proxyUser = stringPreferencesKey("proxy_user")
        val proxyPass = stringPreferencesKey("proxy_pass")
    }

    suspend fun load(): StoredSettings {
        val p = context.dataStore.data.first()
        val mode = runCatching { SourceMode.valueOf(p[K.mode] ?: "XTREAM") }.getOrDefault(SourceMode.XTREAM)
        return StoredSettings(
            config = IPTVConfig(
                mode,
                XtreamConfig(p[K.server].orEmpty(), p[K.user].orEmpty(), p[K.pass].orEmpty()),
                p[K.m3u].orEmpty()
            ),
            sportmonksToken = p[K.sport].orEmpty(),
            favorites = p[K.fav] ?: emptySet(),
            proxy = ProxyConfig(
                enabled = p[K.proxyEnabled] ?: false,
                host = p[K.proxyHost].orEmpty(),
                port = p[K.proxyPort] ?: 8080,
                username = p[K.proxyUser].orEmpty(),
                password = p[K.proxyPass].orEmpty()
            )
        )
    }

    suspend fun save(config: IPTVConfig, sport: String, favorites: Set<String>, proxy: ProxyConfig) {
        context.dataStore.edit { p ->
            p[K.mode] = config.mode.name
            p[K.server] = config.xtream.server
            p[K.user] = config.xtream.username
            p[K.pass] = config.xtream.password
            p[K.m3u] = config.m3uUrl
            p[K.sport] = sport
            p[K.fav] = favorites
            p[K.proxyEnabled] = proxy.enabled
            p[K.proxyHost] = proxy.host
            p[K.proxyPort] = proxy.port
            p[K.proxyUser] = proxy.username
            p[K.proxyPass] = proxy.password
        }
    }
}

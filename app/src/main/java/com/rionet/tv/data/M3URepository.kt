package com.rionet.tv.data

import com.rionet.tv.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class M3URepository {
    suspend fun load(url:String, proxy:ProxyConfig = ProxyConfig()):Catalog=withContext(Dispatchers.IO){
        val client=NetworkClientFactory.create(proxy)
        require(url.startsWith("http")){"أدخل رابط M3U صحيح"}
        val text=client.newCall(Request.Builder().url(url).build()).execute().use { require(it.isSuccessful){"HTTP ${it.code}"}; it.body?.string().orEmpty() }
        val entries=mutableListOf<MediaEntry>(); var meta=""; var index=0
        text.lineSequence().forEach { raw -> val line=raw.trim(); when { line.startsWith("#EXTINF",true)->meta=line; line.isNotBlank()&&!line.startsWith("#")&&meta.isNotBlank()->{
            val title=meta.substringAfterLast(',').trim().ifBlank{"قناة ${index+1}"}; val group=Regex("group-title=\"([^\"]*)\"").find(meta)?.groupValues?.get(1).orEmpty().ifBlank{"غير مصنف"}; val logo=Regex("tvg-logo=\"([^\"]*)\"").find(meta)?.groupValues?.get(1).orEmpty()
            entries += MediaEntry("m3u-${index++}",title,group,group,logo,listOf(line),MediaKind.LIVE,serverOrder=index); meta=""
        } } }
        Catalog(live=entries)
    }
}

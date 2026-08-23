package com.rionet.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.rionet.tv.model.*
import com.rionet.tv.ui.*

@Composable
fun SettingsScreen(vm: RioNetViewModel, s: UiState) {
    var server by remember(s.config) { mutableStateOf(s.config.xtream.server) }
    var user by remember(s.config) { mutableStateOf(s.config.xtream.username) }
    var pass by remember(s.config) { mutableStateOf(s.config.xtream.password) }
    var token by remember(s.sportmonksToken) { mutableStateOf(s.sportmonksToken) }

    var proxyEnabled by remember(s.proxy) { mutableStateOf(s.proxy.enabled) }
    var proxyHost by remember(s.proxy) { mutableStateOf(s.proxy.host) }
    var proxyPort by remember(s.proxy) { mutableStateOf(s.proxy.port.toString()) }
    var proxyUser by remember(s.proxy) { mutableStateOf(s.proxy.username) }
    var proxyPass by remember(s.proxy) { mutableStateOf(s.proxy.password) }

    fun currentProxy() = ProxyConfig(
        enabled = proxyEnabled,
        host = proxyHost.trim().removePrefix("http://").removePrefix("https://").trimEnd('/'),
        port = proxyPort.toIntOrNull() ?: 0,
        username = proxyUser,
        password = proxyPass
    )

    BackHandler { vm.settings(false) }

    Row(
        Modifier.fillMaxSize().padding(horizontal = 64.dp, vertical = 44.dp),
        horizontalArrangement = Arrangement.spacedBy(42.dp)
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("الإعدادات", fontSize = 36.sp)
            Text("RioNet TV • Android TV / Receiver v1.0.4 RC4", fontSize = 16.sp)

            Text("بيانات الخدمة", fontSize = 22.sp, modifier = Modifier.padding(top = 10.dp))
            TextField(value = server, onValueChange = { server = it }, label = { androidx.compose.material3.Text("Xtream Server") })
            TextField(value = user, onValueChange = { user = it }, label = { androidx.compose.material3.Text("Username") })
            TextField(value = pass, onValueChange = { pass = it }, label = { androidx.compose.material3.Text("Password") }, visualTransformation = PasswordVisualTransformation())
            TextField(value = token, onValueChange = { token = it }, label = { androidx.compose.material3.Text("Sportmonks Token") })
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("الشبكة والبروكسي", fontSize = 26.sp)
            Text(
                if (proxyEnabled) "البروكسي مفعّل — سيتم استخدامه للكتالوج وتشغيل الفيديو" else "اتصال مباشر — البروكسي متوقف",
                fontSize = 16.sp
            )

            Button(onClick = { proxyEnabled = !proxyEnabled }) {
                Text(if (proxyEnabled) "إيقاف البروكسي" else "تفعيل البروكسي")
            }

            TextField(value = proxyHost, onValueChange = { proxyHost = it }, label = { androidx.compose.material3.Text("Proxy Host / IP") })
            TextField(value = proxyPort, onValueChange = { proxyPort = it.filter(Char::isDigit).take(5) }, label = { androidx.compose.material3.Text("Port — مثال 8080") })
            TextField(value = proxyUser, onValueChange = { proxyUser = it }, label = { androidx.compose.material3.Text("Proxy Username — اختياري") })
            TextField(value = proxyPass, onValueChange = { proxyPass = it }, label = { androidx.compose.material3.Text("Proxy Password — اختياري") }, visualTransformation = PasswordVisualTransformation())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = !s.testingProxy,
                    onClick = {
                        vm.updateConfig(IPTVConfig(SourceMode.XTREAM, XtreamConfig(server, user, pass)), token, currentProxy())
                        vm.testProxy(currentProxy())
                    }
                ) { Text(if (s.testingProxy) "جاري الاختبار…" else "اختبار البروكسي") }

                Button(onClick = {
                    vm.updateConfig(IPTVConfig(SourceMode.XTREAM, XtreamConfig(server, user, pass)), token, currentProxy())
                    vm.saveAndLoad()
                }) { Text("حفظ وتطبيق") }

                Button(onClick = { vm.settings(false) }) { Text("إلغاء") }
            }

            if (s.message.isNotBlank()) Text(s.message, fontSize = 15.sp)
            if (s.error.isNotBlank()) Text(s.error, fontSize = 15.sp)

            Text(
                "ملاحظة: أدخل اسم المضيف أو IP فقط بدون http://. بيانات البروكسي اختيارية ولا تغيّر بيانات اشتراك Xtream.",
                fontSize = 14.sp
            )
        }
    }
}

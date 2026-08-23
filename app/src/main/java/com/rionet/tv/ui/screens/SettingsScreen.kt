package com.rionet.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.rionet.tv.model.*
import com.rionet.tv.ui.*
import com.rionet.tv.ui.theme.*

private enum class SettingsPane(val title: String) {
    CONNECTION("الحساب والاتصال"), PLAYER("إعدادات المشغل"), ABOUT("حول التطبيق")
}

@Composable
fun SettingsScreen(vm: RioNetViewModel, s: UiState) {
    var pane by remember { mutableStateOf(SettingsPane.PLAYER) }
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

    Column(
        Modifier.fillMaxSize().background(
            Brush.horizontalGradient(listOf(Color(0xFF070A10), Color(0xFF0B1020)))
        )
    ) {
        SettingsTopBar { vm.settings(false) }
        Row(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSidebar(pane, { pane = it }, Modifier.width(360.dp).fillMaxHeight())
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Text(pane.title, color = RioTextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(22.dp))
                when (pane) {
                    SettingsPane.CONNECTION -> ConnectionPane(
                        s = s,
                        server = server, onServer = { server = it },
                        user = user, onUser = { user = it },
                        pass = pass, onPass = { pass = it },
                        token = token, onToken = { token = it },
                        proxyEnabled = proxyEnabled, onProxyEnabled = { proxyEnabled = it },
                        proxyHost = proxyHost, onProxyHost = { proxyHost = it },
                        proxyPort = proxyPort, onProxyPort = { proxyPort = it.filter(Char::isDigit).take(5) },
                        proxyUser = proxyUser, onProxyUser = { proxyUser = it },
                        proxyPass = proxyPass, onProxyPass = { proxyPass = it },
                        onTest = {
                            vm.updateConfig(IPTVConfig(SourceMode.XTREAM, XtreamConfig(server, user, pass)), token, currentProxy())
                            vm.testProxy(currentProxy())
                        },
                        onSave = {
                            vm.updateConfig(IPTVConfig(SourceMode.XTREAM, XtreamConfig(server, user, pass)), token, currentProxy())
                            vm.saveAndLoad()
                        }
                    )
                    SettingsPane.PLAYER -> PlayerSettingsPane()
                    SettingsPane.ABOUT -> AboutPane()
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onClose: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(92.dp).background(Color(0xF205070C)).border(0.dp, Color.Transparent).padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FocusSettingsButton("رجوع", false, onClose)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.width(6.dp).height(52.dp).background(RioPrimary, RoundedCornerShape(3.dp)))
            Text("RioNet TV", color = RioTextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsSidebar(active: SettingsPane, onSelect: (SettingsPane) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.background(Color(0xCC0B1020), RoundedCornerShape(24.dp)).border(1.dp, BorderDefault, RoundedCornerShape(24.dp)).padding(20.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().height(88.dp).background(
                Brush.horizontalGradient(listOf(RioPrimary, BlueAction)), RoundedCornerShape(18.dp)
            ).padding(18.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text("RioNet TV", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("إعدادات Android TV", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        SettingsPane.entries.forEach { item ->
            SettingsMenuItem(item.title, item == active) { onSelect(item) }
            Spacer(Modifier.height(10.dp))
        }
        SettingsMenuItem("الواجهة واللغة", false) { }
        Spacer(Modifier.height(10.dp))
        SettingsMenuItem("الدعم والمساعدة", false) { }
    }
}

@Composable
private fun SettingsMenuItem(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().height(56.dp).scale(if (focused) 1.02f else 1f).onFocusChanged { focused = it.isFocused }
            .background(if (selected) Color(0x1A8A4DFF) else Color(0xCC0F1628), shape)
            .border(if (focused) 3.dp else 1.dp, if (focused || selected) RioPrimary else BorderDefault, shape)
            .clickable(onClick = onClick).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = if (selected) RioTextPrimary else RioTextSecondary, fontSize = 17.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        if (selected) Box(Modifier.width(6.dp).height(24.dp).background(RioPrimary, RoundedCornerShape(99.dp)))
    }
}

@Composable
private fun PlayerSettingsPane() {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("خيارات الصوت والترجمة", color = RioTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            SettingValueCard("جودة البث الافتراضية", "تلقائي — أعلى جودة مستقرة", "AUTO")
            SettingValueCard("لغة الصوت المفضلة", "العربية عند توفرها", "AR")
            SettingValueCard("لغة الترجمة المفضلة", "العربية عند توفرها", "CC")
            InfoCard("مهم", "تغيير الجودة يدويًا يعتمد على وجود مسارات متعددة داخل البث نفسه. لا يتم عرض خيارات وهمية إذا كان المزود يرسل مسارًا واحدًا فقط.")
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("إعدادات التشغيل الذكي", color = RioTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            SmartSetting("جودة البث التلقائية", "يتولى ExoPlayer اختيار المسار المناسب عند توفر بث Adaptive.", true)
            SmartSetting("تشغيل الحلقة التالية تلقائيًا", "سيتم تفعيله بعد إضافة تسلسل حلقات للمشغل.", false, enabled = false)
            SmartSetting("تخطي المقدمة تلقائيًا", "يتطلب بيانات زمنية من مزود المحتوى؛ غير متاحة حاليًا.", false, enabled = false)
            SmartSetting("الصوت المحيطي", "يمرر الصوت حسب دعم الملف والجهاز دون فرض تنسيق غير موجود.", true)
        }
    }
}

@Composable
private fun SettingValueCard(label: String, value: String, badge: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, color = RioTextSecondary, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().height(64.dp).background(Control, RoundedCornerShape(12.dp)).border(1.dp, BorderDefault, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(badge, color = RioPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(value, color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun SmartSetting(title: String, description: String, checked: Boolean, enabled: Boolean = true) {
    Row(
        Modifier.fillMaxWidth().height(86.dp).background(Color(0xCC151C2C), RoundedCornerShape(16.dp)).border(1.dp, BorderDefault, RoundedCornerShape(16.dp)).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ToggleVisual(checked = checked, enabled = enabled)
        Column(Modifier.weight(1f).padding(start = 18.dp), horizontalAlignment = Alignment.End) {
            Text(title, color = if (enabled) RioTextPrimary else RioTextSecondary, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            Spacer(Modifier.height(4.dp))
            Text(description, color = RioTextSecondary, fontSize = 13.sp, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun ToggleVisual(checked: Boolean, enabled: Boolean) {
    val track = when {
        !enabled -> Color(0xFF2A354C)
        checked -> RioPrimary
        else -> Color(0xFF344158)
    }
    Box(Modifier.width(48.dp).height(26.dp).background(track, RoundedCornerShape(99.dp))) {
        Box(
            Modifier.size(20.dp).align(if (checked) Alignment.CenterEnd else Alignment.CenterStart).padding(3.dp)
                .background(if (enabled) Color.White else RioTextSecondary, RoundedCornerShape(99.dp))
        )
    }
}

@Composable
private fun ConnectionPane(
    s: UiState,
    server: String, onServer: (String) -> Unit,
    user: String, onUser: (String) -> Unit,
    pass: String, onPass: (String) -> Unit,
    token: String, onToken: (String) -> Unit,
    proxyEnabled: Boolean, onProxyEnabled: (Boolean) -> Unit,
    proxyHost: String, onProxyHost: (String) -> Unit,
    proxyPort: String, onProxyPort: (String) -> Unit,
    proxyUser: String, onProxyUser: (String) -> Unit,
    proxyPass: String, onProxyPass: (String) -> Unit,
    onTest: () -> Unit,
    onSave: () -> Unit
) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("بيانات الخدمة", color = RioTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextField(server, onServer, label = { androidx.compose.material3.Text("Xtream Server") })
            TextField(user, onUser, label = { androidx.compose.material3.Text("Username") })
            TextField(pass, onPass, label = { androidx.compose.material3.Text("Password") }, visualTransformation = PasswordVisualTransformation())
            TextField(token, onToken, label = { androidx.compose.material3.Text("Sportmonks Token") })
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("الشبكة والبروكسي", color = RioTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            FocusSettingsButton(if (proxyEnabled) "إيقاف البروكسي" else "تفعيل البروكسي", proxyEnabled) { onProxyEnabled(!proxyEnabled) }
            TextField(proxyHost, onProxyHost, label = { androidx.compose.material3.Text("Proxy Host / IP") })
            TextField(proxyPort, onProxyPort, label = { androidx.compose.material3.Text("Port") })
            TextField(proxyUser, onProxyUser, label = { androidx.compose.material3.Text("Proxy Username — اختياري") })
            TextField(proxyPass, onProxyPass, label = { androidx.compose.material3.Text("Proxy Password — اختياري") }, visualTransformation = PasswordVisualTransformation())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(enabled = !s.testingProxy, onClick = onTest) { Text(if (s.testingProxy) "جاري الاختبار…" else "اختبار البروكسي") }
                Button(onClick = onSave) { Text("حفظ وتطبيق") }
            }
            if (s.message.isNotBlank()) Text(s.message, color = RioSuccess, fontSize = 14.sp)
            if (s.error.isNotBlank()) Text(s.error, color = RioError, fontSize = 14.sp)
        }
    }
}

@Composable
private fun AboutPane() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("RioNet TV", color = RioTextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Android TV • RC5", color = RioTextSecondary, fontSize = 18.sp)
        InfoCard("البنية الحالية", "Jetpack Compose for TV + Media3 ExoPlayer + Xtream/M3U + Proxy اختياري. التصميم ينتقل تدريجيًا من Figma إلى الكود الحقيقي.")
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Column(
        Modifier.fillMaxWidth().background(Color(0xCC151C2C), RoundedCornerShape(16.dp)).border(1.dp, BorderDefault, RoundedCornerShape(16.dp)).padding(18.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(title, color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(body, color = RioTextSecondary, fontSize = 14.sp, lineHeight = 22.sp, textAlign = TextAlign.End)
    }
}

@Composable
private fun FocusSettingsButton(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.height(56.dp).scale(if (focused) 1.04f else 1f).onFocusChanged { focused = it.isFocused }
            .background(if (selected) RioPrimary else ListSurface, shape)
            .border(if (focused) 3.dp else 1.dp, if (focused) Color.White else BorderDefault, shape)
            .clickable(onClick = onClick).padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = RioTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
}

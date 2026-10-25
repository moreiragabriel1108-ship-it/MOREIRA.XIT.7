package com.luaxit.sevenx

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Bg = Color(0xFF08090C)
private val CardBg = Color(0xFF15171D)
private val Card2 = Color(0xFF20232B)
private val Red = Color(0xFFFF2638)
private val Muted = Color(0xFF9BA3B2)
private val Green = Color(0xFF42D7A0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("moreira7", MODE_PRIVATE)
        setContent {
            val context = LocalContext.current
            var tab by remember { mutableIntStateOf(0) }
            var sensitivity by remember { mutableIntStateOf(prefs.getInt("sensitivity", 85).coerceIn(40, 100)) }
            var capa by remember { mutableIntStateOf(prefs.getInt("capa", 75).coerceIn(40, 100)) }
            var popupEnabled by remember { mutableStateOf(prefs.getBoolean("popup_enabled", true)) }
            var tipsEnabled by remember { mutableStateOf(prefs.getBoolean("tips_enabled", true)) }
            var compactPopups by remember { mutableStateOf(prefs.getBoolean("compact_popups", false)) }
            var active by remember { mutableStateOf(false) }
            var seconds by remember { mutableIntStateOf(30) }
            var hits by remember { mutableIntStateOf(prefs.getInt("hits", 0)) }
            var sessions by remember { mutableIntStateOf(prefs.getInt("sessions", 0)) }
            var best by remember { mutableIntStateOf(prefs.getInt("best", 0)) }
            var roundHits by remember { mutableIntStateOf(0) }
            var targetX by remember { mutableFloatStateOf(.3f) }
            var targetY by remember { mutableFloatStateOf(.3f) }
            var showPopup by remember { mutableStateOf(false) }
            var popupTitle by remember { mutableStateOf("MOREIRA7") }
            var popupMessage by remember { mutableStateOf("Configuração salva.") }
            var ramAvailable by remember { mutableStateOf("—") }
            var ramTotal by remember { mutableStateOf("—") }
            var battery by remember { mutableIntStateOf(-1) }
            var refreshRate by remember { mutableStateOf("—") }
            var deviceCores by remember { mutableIntStateOf(Runtime.getRuntime().availableProcessors()) }
            var deviceStatus by remember { mutableStateOf("Toque em ATUALIZAR LEITURA para consultar o aparelho.") }

            fun notify(title: String, message: String) {
                popupTitle = title
                popupMessage = message
                if (popupEnabled) showPopup = true
            }
            fun readDevice() {
                val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val info = ActivityManager.MemoryInfo()
                manager.getMemoryInfo(info)
                ramAvailable = String.format("%.1f GB", info.availMem / 1073741824.0)
                ramTotal = String.format("%.1f GB", info.totalMem / 1073741824.0)
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
                @Suppress("DEPRECATION")
                refreshRate = String.format("%.0f Hz", windowManager.defaultDisplay.refreshRate)
                deviceCores = Runtime.getRuntime().availableProcessors()
                deviceStatus = if (info.lowMemory) "Memória do sistema está baixa." else "Memória do sistema dentro do estado normal."
            }
            fun openSettings(action: String) {
                try { context.startActivity(Intent(action)) }
                catch (_: Exception) { notify("Ajuste indisponível", "Abra as Configurações do Android manualmente.") }
            }

            LaunchedEffect(active) {
                if (active) {
                    seconds = 30
                    roundHits = 0
                    while (seconds > 0 && active) {
                        delay(1000)
                        seconds--
                    }
                    if (active) {
                        active = false
                        sessions++
                        if (roundHits > best) best = roundHits
                        prefs.edit().putInt("sessions", sessions).putInt("best", best).apply()
                        notify("Rodada concluída", "Acertos: $roundHits • Recorde: $best")
                    }
                }
            }

            MaterialTheme(colorScheme = darkColorScheme(primary = Red, secondary = Green, background = Bg, surface = CardBg)) {
                Scaffold(
                    containerColor = Bg,
                    bottomBar = {
                        NavigationBar(containerColor = CardBg) {
                            val names = listOf("Painel", "Sensib.", "Otimizar", "Pop-ups", "Ajustes")
                            val icons = listOf("◆", "◎", "ϟ", "▣", "⚙")
                            names.forEachIndexed { i, name ->
                                NavigationBarItem(
                                    selected = tab == i, onClick = { tab = i },
                                    icon = { Text(icons[i], fontSize = 18.sp) }, label = { Text(name, fontSize = 9.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Red, selectedTextColor = Red, indicatorColor = Red.copy(alpha = .13f), unselectedIconColor = Muted, unselectedTextColor = Muted)
                                )
                            }
                        }
                    }
                ) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxWidth().height(150.dp).background(
                                Brush.linearGradient(listOf(Color(0xFF39050B), Color(0xFF16090D), Color(0xFF07080B))),
                                RoundedCornerShape(22.dp)
                            ).padding(16.dp)
                        ) {
                            Column(Modifier.align(Alignment.CenterStart)) {
                                Text("MOREIRA7", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                Text("GAMING CONTROL CENTER", color = Red, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                                Text("PERFIS  •  DESEMPENHO  •  FERRAMENTAS", color = Muted, fontSize = 9.sp)
                            }
                            Text("☠", Modifier.align(Alignment.CenterEnd), color = Red, fontSize = 65.sp)
                            Text("🔥", Modifier.align(Alignment.TopEnd), fontSize = 28.sp)
                            Text("🔥", Modifier.align(Alignment.BottomEnd), fontSize = 24.sp)
                        }

                        when (tab) {
                            0 -> {
                                Text("PAINEL PRINCIPAL", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                                Text("Atalhos rápidos para preparar sua sessão de jogo.", color = Muted, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Metric("ACERTOS", hits.toString(), Modifier.weight(1f))
                                    Metric("RODADAS", sessions.toString(), Modifier.weight(1f))
                                    Metric("RECORDE", best.toString(), Modifier.weight(1f))
                                }
                                Panel("PERFIL ATUAL") {
                                    Text("Sensibilidade: $sensitivity / 100", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Perfil de capa: $capa%", color = Red, fontWeight = FontWeight.Bold)
                                    Button(onClick = { tab = 1 }, modifier = Modifier.fillMaxWidth()) { Text("EDITAR PERFIL") }
                                }
                                Panel("ATALHOS DE JOGO") {
                                    ActionButton("Abrir configurações de tela", "Ajustar taxa de atualização e brilho") { openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                                    ActionButton("Abrir configurações de bateria", "Consultar modo de energia do aparelho") { openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
                                    ActionButton("Abrir MOREIRA7 nas configurações", "Armazenamento e permissões do aplicativo") {
                                        openSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    }
                                }
                            }
                            1 -> {
                                Text("SENSIBILIDADE", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                                Text("Monte seu perfil pessoal e copie os valores para usar como referência.", color = Muted, fontSize = 12.sp)
                                Panel("SENSIBILIDADE GERAL") {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("$sensitivity", color = Red, fontSize = 42.sp, fontWeight = FontWeight.Black)
                                        Text(" / 100", color = Muted, modifier = Modifier.padding(bottom = 7.dp))
                                    }
                                    Slider(value = sensitivity.toFloat(), onValueChange = { sensitivity = it.toInt() }, valueRange = 40f..100f, onValueChangeFinished = { prefs.edit().putInt("sensitivity", sensitivity).apply() })
                                    Presets(listOf(40, 50, 60, 70, 80, 90, 100)) { sensitivity = it; prefs.edit().putInt("sensitivity", it).apply() }
                                }
                                Panel("PERFIL DE CAPA") {
                                    Text("$capa%", color = Red, fontSize = 38.sp, fontWeight = FontWeight.Black)
                                    Slider(value = capa.toFloat(), onValueChange = { capa = it.toInt() }, valueRange = 40f..100f, onValueChangeFinished = { prefs.edit().putInt("capa", capa).apply() })
                                    Presets(listOf(40, 50, 60, 70, 80, 90, 100)) { capa = it; prefs.edit().putInt("capa", it).apply() }
                                }
                                Button(
                                    onClick = {
                                        val profile = "MOREIRA7 | Sensibilidade: $sensitivity/100 | Perfil de capa: $capa%"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Perfil MOREIRA7", profile))
                                        notify("Perfil copiado", profile)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("COPIAR PERFIL") }
                            }
                            2 -> {
                                Text("OTIMIZAÇÃO", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                                Text("Leituras reais do aparelho e atalhos para ajustes do sistema.", color = Muted, fontSize = 12.sp)
                                Panel("MONITOR DO DISPOSITIVO") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Metric("RAM LIVRE", ramAvailable, Modifier.weight(1f))
                                        Metric("RAM TOTAL", ramTotal, Modifier.weight(1f))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Metric("TELA", refreshRate, Modifier.weight(1f))
                                        Metric("NÚCLEOS CPU", deviceCores.toString(), Modifier.weight(1f))
                                    }
                                    Metric("BATERIA", if (battery >= 0) "$battery%" else "Indisponível", Modifier.fillMaxWidth())
                                    Text(deviceStatus, color = Muted, fontSize = 12.sp)
                                    Button(onClick = { readDevice() }, modifier = Modifier.fillMaxWidth()) { Text("ATUALIZAR LEITURA") }
                                }
                                Panel("AJUSTES DE DESEMPENHO") {
                                    ActionButton("CPU • modo de energia", "Abrir ajustes de bateria e desempenho") { openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
                                    ActionButton("GPU • qualidade e fluidez", "Abrir configurações de tela") { openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                                    ActionButton("RAM • armazenamento do app", "Abrir armazenamento para limpar o cache do MOREIRA7") { openSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) }
                                    ActionButton("Modo Não Perturbe", "Configurar interrupções durante a partida") { openSettings(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS) }
                                    ActionButton("Game Booster / modo de jogo", "Procure o recurso de jogos integrado do fabricante") { openSettings(Settings.ACTION_SETTINGS) }
                                }
                                Text("Os atalhos abrem configurações do Android; as opções disponíveis dependem do fabricante e da versão do sistema.", color = Muted, fontSize = 11.sp)
                            }
                            3 -> {
                                Text("POP-UPS", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                                Text("Personalize os avisos que aparecem dentro do MOREIRA7.", color = Muted, fontSize = 12.sp)
                                Panel("ESTILO DE AVISOS") {
                                    PrefRow("Pop-ups ativados", "Mostrar confirmações e resultados", popupEnabled) { popupEnabled = it; prefs.edit().putBoolean("popup_enabled", it).apply() }
                                    PrefRow("Dicas rápidas", "Mostrar instruções e sugestões", tipsEnabled) { tipsEnabled = it; prefs.edit().putBoolean("tips_enabled", it).apply() }
                                    PrefRow("Pop-up compacto", "Usar textos curtos nas mensagens", compactPopups) { compactPopups = it; prefs.edit().putBoolean("compact_popups", it).apply() }
                                    Button(onClick = { notify("Teste de pop-up", if (compactPopups) "MOREIRA7 • Aviso de teste" else "Se você está vendo esta janela, os pop-ups internos estão ativos.") }, modifier = Modifier.fillMaxWidth()) { Text("TESTAR POP-UP") }
                                }
                                Panel("AÇÕES RÁPIDAS") {
                                    ActionButton("Resumo do perfil", "Ver os valores salvos") { notify("Perfil salvo", "Sensibilidade $sensitivity/100 • Capa $capa%") }
                                    ActionButton("Resumo do aparelho", "Consultar leitura atual") { readDevice(); notify("Estado do aparelho", "RAM livre $ramAvailable de $ramTotal • Tela $refreshRate • Bateria $battery%") }
                                }
                            }
                            else -> {
                                Text("AJUSTES", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                                Panel("PREFERÊNCIAS") {
                                    ActionButton("Restaurar perfil", "Sensibilidade 85 • Capa 75") {
                                        sensitivity = 85; capa = 75
                                        prefs.edit().putInt("sensitivity", 85).putInt("capa", 75).apply()
                                        notify("Perfil restaurado", "Sensibilidade 85 • Capa 75")
                                    }
                                    ActionButton("Limpar cache do app", "Apagar arquivos temporários do MOREIRA7") {
                                        val files = context.cacheDir.listFiles()?.toList().orEmpty()
                                        val removed = files.count { it.deleteRecursively() }
                                        notify("Cache processado", "$removed item(ns) temporário(s) removido(s).")
                                    }
                                    ActionButton("Sobre MOREIRA7", "Informações da versão") { notify("MOREIRA7", "Gaming Control Center • versão 2.0") }
                                }
                                Panel("INFORMAÇÕES") {
                                    Text("Versão Android: ${Build.VERSION.RELEASE}", color = Color.White)
                                    Text("SDK: ${Build.VERSION.SDK_INT}", color = Muted)
                                    Text("Arquitetura: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "desconhecida"}", color = Muted)
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                    }
                }
                if (showPopup) {
                    AlertDialog(
                        onDismissRequest = { showPopup = false },
                        containerColor = CardBg,
                        title = { Text(popupTitle, color = Red, fontWeight = FontWeight.Black) },
                        text = { Text(if (compactPopups) popupMessage.take(70) else popupMessage, color = Color.White) },
                        confirmButton = { TextButton(onClick = { showPopup = false }) { Text("OK", color = Red) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun Panel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text(title, color = Red, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
            content()
        }
    }
}
@Composable
private fun Metric(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Card2), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}
@Composable
private fun ActionButton(title: String, subtitle: String, action: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Card2, RoundedCornerShape(12.dp)).clickable(onClick = action).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, color = Muted, fontSize = 10.sp)
        }
        Text("›", color = Red, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
private fun Presets(values: List<Int>, choose: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        values.forEach { value ->
            Text(
                "$value",
                Modifier.weight(1f).background(if (value == values[values.size / 2]) Red.copy(alpha = .18f) else Card2, RoundedCornerShape(8.dp))
                    .clickable { choose(value) }.padding(vertical = 9.dp),
                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
@Composable
private fun PrefRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 10.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

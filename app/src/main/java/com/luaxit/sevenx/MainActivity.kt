package com.luaxit.sevenx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Background = Color(0xFF090A0D)
private val SurfaceDark = Color(0xFF14161B)
private val SurfaceRaised = Color(0xFF1C2027)
private val Accent = Color(0xFFFF303B)
private val TextMuted = Color(0xFF9BA3B2)
private val Green = Color(0xFF47D7A0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("lua_xit", MODE_PRIVATE)
        setContent {
            var tab by remember { mutableIntStateOf(0) }
            var sensitivity by remember { mutableIntStateOf(prefs.getInt("sensitivity", 80).coerceIn(1, 100)) }
            var targetSize by remember { mutableIntStateOf(prefs.getInt("target_size", 50).coerceIn(25, 80)) }
            var sound by remember { mutableStateOf(prefs.getBoolean("sound", false)) }
            var vibration by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
            var showGuide by remember { mutableStateOf(prefs.getBoolean("guide", true)) }
            var hits by remember { mutableIntStateOf(prefs.getInt("total_hits", 0)) }
            var sessions by remember { mutableIntStateOf(prefs.getInt("sessions", 0)) }
            var best by remember { mutableIntStateOf(prefs.getInt("best_hits", 0)) }
            var active by remember { mutableStateOf(false) }
            var remaining by remember { mutableIntStateOf(30) }
            var roundHits by remember { mutableIntStateOf(0) }
            var targetX by remember { mutableFloatStateOf(.35f) }
            var targetY by remember { mutableFloatStateOf(.35f) }

            LaunchedEffect(active) {
                if (active) {
                    remaining = 30
                    roundHits = 0
                    while (remaining > 0 && active) {
                        delay(1000)
                        remaining--
                    }
                    if (active) {
                        active = false
                        sessions++
                        if (roundHits > best) {
                            best = roundHits
                            prefs.edit().putInt("best_hits", best).apply()
                        }
                        prefs.edit().putInt("sessions", sessions).apply()
                    }
                }
            }

            MaterialTheme(colorScheme = darkColorScheme(
                primary = Accent, secondary = Green, background = Background,
                surface = SurfaceDark, onBackground = Color.White, onSurface = Color.White
            )) {
                Scaffold(
                    containerColor = Background,
                    bottomBar = {
                        NavigationBar(containerColor = SurfaceDark, contentColor = Color.White) {
                            val labels = listOf("Painel", "Mira", "Treino", "Ajustes")
                            val glyphs = listOf("⌂", "◎", "◉", "⚙")
                            labels.forEachIndexed { index, label ->
                                NavigationBarItem(
                                    selected = tab == index,
                                    onClick = { tab = index },
                                    icon = { Text(glyphs[index], fontSize = 20.sp) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Accent, selectedTextColor = Accent,
                                        indicatorColor = Accent.copy(alpha = .13f),
                                        unselectedIconColor = TextMuted, unselectedTextColor = TextMuted
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).background(Background).padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).background(Accent, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                                Text("LX", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("LUA XIT", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("7X  •  AIM TRAINER", fontSize = 10.sp, color = TextMuted, letterSpacing = 2.sp)
                            }
                            Box(Modifier.background(Green.copy(alpha = .12f), CircleShape).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text("● PRONTO", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        when (tab) {
                            0 -> {
                                Text("Seu painel", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                                Text("Treine reflexo e precisão em uma área de treino local.", color = TextMuted, fontSize = 13.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatCard("MELHOR RODADA", best.toString(), "alvos", Modifier.weight(1f))
                                    StatCard("RODADAS", sessions.toString(), "concluídas", Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatCard("ACERTOS TOTAIS", hits.toString(), "desde a instalação", Modifier.weight(1f))
                                    StatCard("SENSIBILIDADE", "$sensitivity%", "preferência salva", Modifier.weight(1f))
                                }
                                Panel("INÍCIO RÁPIDO") {
                                    Text("Rodada de 30 segundos", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text("Toque nos alvos que aparecem em posições diferentes e acompanhe seus resultados.", color = TextMuted, fontSize = 13.sp)
                                    Button(onClick = { tab = 2 }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                        Text("COMEÇAR TREINO", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("Este app treina apenas dentro dele. Não injeta código nem lê dados do Free Fire.", color = TextMuted, fontSize = 11.sp)
                            }
                            1 -> {
                                Text("Configuração de mira", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("Ajustes reais para a área de treino do aplicativo.", color = TextMuted, fontSize = 13.sp)
                                Panel("SENSIBILIDADE DO TREINO") {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("$sensitivity", color = Accent, fontSize = 38.sp, fontWeight = FontWeight.Black)
                                        Text(" / 100", color = TextMuted, fontSize = 15.sp, modifier = Modifier.padding(bottom = 7.dp))
                                    }
                                    Slider(value = sensitivity.toFloat(), onValueChange = { sensitivity = it.toInt() }, valueRange = 1f..100f, onValueChangeFinished = { prefs.edit().putInt("sensitivity", sensitivity).apply() })
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(25, 50, 75, 100).forEach { value ->
                                            OutlinedButton(onClick = { sensitivity = value; prefs.edit().putInt("sensitivity", value).apply() }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp)) { Text("$value") }
                                        }
                                    }
                                }
                                Panel("TAMANHO DO ALVO") {
                                    Text("$targetSize%", color = Accent, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    Slider(value = targetSize.toFloat(), onValueChange = { targetSize = it.toInt() }, valueRange = 25f..80f, onValueChangeFinished = { prefs.edit().putInt("target_size", targetSize).apply() })
                                    Text("O tamanho altera os alvos do exercício.", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                            2 -> {
                                Text("Campo de treino", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (active) "RODADA EM ANDAMENTO" else "RODADA DE 30 SEGUNDOS", color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    Text("00:${remaining.toString().padStart(2, '0')}", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                                BoxWithConstraints(
                                    Modifier.fillMaxWidth().weight(1f).background(SurfaceRaised, RoundedCornerShape(20.dp)).padding(8.dp)
                                ) {
                                    if (!active) {
                                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text("◎", color = Accent, fontSize = 54.sp)
                                            Text("Pronto para treinar?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            Text("Inicie a rodada para aparecerem os alvos.", color = TextMuted, fontSize = 12.sp)
                                            Button(onClick = { active = true; targetX = .2f; targetY = .2f }, shape = RoundedCornerShape(12.dp)) { Text("INICIAR RODADA") }
                                        }
                                    } else {
                                        Text("ACERTOS  $roundHits", Modifier.align(Alignment.TopStart).padding(8.dp), color = Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            Modifier.offset(x = (maxWidth - targetSize.dp) * targetX, y = (maxHeight - targetSize.dp) * targetY)
                                                .size(targetSize.dp.coerceAtLeast(25.dp))
                                                .background(Accent.copy(alpha = .16f), CircleShape)
                                                .padding(4.dp)
                                                .background(Accent, CircleShape)
                                                .clickable {
                                                    roundHits++
                                                    hits++
                                                    prefs.edit().putInt("total_hits", hits).apply()
                                                    targetX = Random.nextFloat() * .72f
                                                    targetY = Random.nextFloat() * .72f
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(Modifier.fillMaxSize(.52f).background(Color.White, CircleShape))
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(onClick = { if (active) { active = false; sessions++; prefs.edit().putInt("sessions", sessions).apply(); if (roundHits > best) { best = roundHits; prefs.edit().putInt("best_hits", best).apply() } } else { roundHits = 0; remaining = 30 } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                        Text(if (active) "ENCERRAR" else "ZERAR RODADA")
                                    }
                                    OutlinedButton(onClick = { active = false; roundHits = 0; remaining = 30 }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("REINICIAR") }
                                }
                                Text("Pontuação e recordes ficam salvos neste aparelho.", color = TextMuted, fontSize = 11.sp)
                            }
                            else -> {
                                Text("Ajustes", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("Preferências do aplicativo", color = TextMuted, fontSize = 13.sp)
                                Panel("EXPERIÊNCIA") {
                                    PreferenceRow("Som de interação", "Preferência para sons do app", sound) { sound = it; prefs.edit().putBoolean("sound", it).apply() }
                                    PreferenceRow("Vibração", "Preferência para feedback tátil", vibration) { vibration = it; prefs.edit().putBoolean("vibration", it).apply() }
                                    PreferenceRow("Dicas no treino", "Exibe instruções na área de treino", showGuide) { showGuide = it; prefs.edit().putBoolean("guide", it).apply() }
                                }
                                Panel("DADOS LOCAIS") {
                                    Text("Android ${android.os.Build.VERSION.RELEASE} • SDK ${android.os.Build.VERSION.SDK_INT}", color = TextMuted, fontSize = 12.sp)
                                    Text("$hits acertos • $sessions rodadas • recorde $best", color = Color.White, fontSize = 13.sp)
                                    OutlinedButton(onClick = {
                                        sensitivity = 80; targetSize = 50; sound = false; vibration = true; showGuide = true; hits = 0; sessions = 0; best = 0; active = false; roundHits = 0; remaining = 30
                                        prefs.edit().clear().apply()
                                        prefs.edit().putInt("sensitivity", 80).putInt("target_size", 50).putBoolean("sound", false).putBoolean("vibration", true).putBoolean("guide", true).apply()
                                    }, modifier = Modifier.fillMaxWidth()) { Text("RESTAURAR E APAGAR ESTATÍSTICAS") }
                                }
                                Panel("SOBRE") {
                                    Text("LUA XIT 7x • versão 1.1", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Ferramenta de treino independente. Não oferece anti-ban, não modifica o jogo e não promete integração com Free Fire.", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Panel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            content()
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
            Text(value, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            Text(detail, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PreferenceRow(title: String, detail: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(detail, color = TextMuted, fontSize = 11.sp)
        }
        Switch(checked = value, onCheckedChange = onChange)
    }
}

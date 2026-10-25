package com.luaxit.sevenx
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val bg=Color(0xFF101010); private val panel=Color(0xFF1C1C1C); private val red=Color(0xFFE53935)
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 val prefs=getSharedPreferences("lua_xit",MODE_PRIVATE)
 setContent {
 var tab by remember { mutableIntStateOf(0) }
 var precision by remember { mutableFloatStateOf(prefs.getInt("precision",80).coerceIn(40,100).toFloat()) }
 var markers by remember { mutableStateOf(prefs.getBoolean("markers",true)) }
 var boxes by remember { mutableStateOf(prefs.getBoolean("boxes",true)) }
 var lines by remember { mutableStateOf(prefs.getBoolean("lines",false)) }
 var hits by remember { mutableIntStateOf(0) }
 MaterialTheme(colorScheme=darkColorScheme(primary=red,background=bg,surface=panel)){
 Scaffold(containerColor=bg,bottomBar={NavigationBar(containerColor=panel){listOf("Início","Mira","Treino","Config").forEachIndexed{i,s->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(listOf("⌂","◎","◉","⚙")[i])},label={Text(s)})}}}){pad->
 Column(Modifier.fillMaxSize().background(bg).padding(pad).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
 Text("LUA XIT 7x",color=red,fontSize=28.sp); Text("PAINEL DE TREINAMENTO",color=Color.LightGray,fontSize=12.sp)
 when(tab){
 0->{PanelCard("STATUS","Aplicativo pronto. Android "+android.os.Build.VERSION.RELEASE+"\nTreinamento independente; sem ligação ao Free Fire.");PanelCard("PRECISÃO",precision.toInt().toString()+"% — valor usado apenas no painel de treino.")}
 1->{PanelCard("PRECISÃO DA MIRA","Ajuste de preferência para os exercícios deste aplicativo.");Text(precision.toInt().toString()+"%",color=red,fontSize=30.sp);Slider(value=precision,onValueChange={precision=it},valueRange=40f..100f,onValueChangeFinished={prefs.edit().putInt("precision",precision.toInt()).apply()});Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf(40,50,60,70,80,90,100).forEach{v->Button(onClick={precision=v.toFloat();prefs.edit().putInt("precision",v).apply()},contentPadding=PaddingValues(8.dp)){Text(v.toString())}}}}
 2->{PanelCard("CAMPO DE TREINO","Toque nos alvos simulados. Não detecta jogadores do Free Fire.");BoxWithConstraints(Modifier.fillMaxWidth().height(300.dp).background(Color(0xFF20252A),RoundedCornerShape(16.dp)).padding(8.dp)){val ps=listOf(.12f to .18f,.62f to .24f,.32f to .58f,.72f to .72f);ps.forEachIndexed{i,p->Column(Modifier.offset(x=maxWidth*p.first,y=maxHeight*p.second),horizontalAlignment=Alignment.CenterHorizontally){if(markers)Text("ALVO "+(i+1),color=red,fontSize=10.sp);if(boxes)Text("[  ⊕  ]",color=red);if(lines)Text("│",color=red);Button(onClick={hits++},contentPadding=PaddingValues(2.dp)){Text("●")}}}};Text("Acertos: "+hits,color=Color.White);Button(onClick={hits=0}){Text("Reiniciar")};ToggleLine("Marcadores",markers){markers=it;prefs.edit().putBoolean("markers",it).apply()};ToggleLine("Caixas",boxes){boxes=it;prefs.edit().putBoolean("boxes",it).apply()};ToggleLine("Linhas",lines){lines=it;prefs.edit().putBoolean("lines",it).apply()}}
 else->{PanelCard("DIAGNÓSTICO","Android "+android.os.Build.VERSION.RELEASE+" • SDK "+android.os.Build.VERSION.SDK_INT+"\nSem root ou permissões especiais.");Button(onClick={precision=80f;markers=true;boxes=true;lines=false;hits=0;prefs.edit().clear().apply()}){Text("Restaurar padrão")};Text("Sem promessa de anti-ban. O app não modifica jogos de terceiros.",color=Color.LightGray)}
 }
 }
 }}
 }
 }
}
@Composable private fun PanelCard(title:String,body:String){Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,color=red);Text(body,color=Color.White)}}}
@Composable private fun ToggleLine(label:String,value:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f),color=Color.White);Switch(checked=value,onCheckedChange=onChange)}}

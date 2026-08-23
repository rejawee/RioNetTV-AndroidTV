package com.rionet.tv.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.rionet.tv.ui.*
import com.rionet.tv.ui.components.FocusTile
@Composable fun MatchesScreen(vm:RioNetViewModel,s:UiState){Column(Modifier.fillMaxSize().padding(48.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("المباريات المباشرة",fontSize=34.sp);Button(onClick=vm::refreshMatches){Text("تحديث")}};Spacer(Modifier.height(18.dp));if(s.sportmonksToken.isBlank())Text("أدخل Sportmonks API Token من الإعدادات",fontSize=20.sp) else LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(s.matches,key={it.id}){m->FocusTile(false,{}){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(m.name,fontSize=20.sp);Text("${m.homeScore} - ${m.awayScore}")}}}}}}

package com.rionet.tv
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rionet.tv.ui.RioNetViewModel
import com.rionet.tv.ui.RootScreen
import com.rionet.tv.ui.theme.RioNetTheme
class MainActivity:ComponentActivity(){private val vm:RioNetViewModel by viewModels();override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{RioNetTheme{RootScreen(vm)}}}}

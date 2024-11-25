package com.example.connectivityapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun ConnectivityScreen(
    modifier: Modifier, connectivityViewModel: ConnectivityViewModel = hiltViewModel()
) {
    
    val context = LocalContext.current
    
    val connectionType = getConnectionType(context)
    
    val coroutineScope = rememberCoroutineScope()
    
    var signalStrength by remember {
        mutableIntStateOf(-1)
    }
    
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val signalWifiStrength = getWifiSignalStrength(context)
            signalStrength = signalWifiStrength
        }
    }
    
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text("You're currently online with: $connectionType ")
        Text("Your Signal strength: $signalStrength ")
    }
}
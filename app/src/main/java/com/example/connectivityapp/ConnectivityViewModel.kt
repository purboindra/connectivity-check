package com.example.connectivityapp

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _connectionStatus = MutableStateFlow("No Connection")
    val connectionStatus: StateFlow<String> = _connectionStatus
    
    init {
        viewModelScope.launch {
            getApplication<Application>().applicationContext.observeConnectivity()
                .collectLatest { connectionType ->
                    _connectionStatus.emit(connectionType)
                }
        }
    }
}
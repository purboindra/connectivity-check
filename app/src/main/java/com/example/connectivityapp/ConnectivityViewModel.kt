package com.example.connectivityapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    
    private val _connectionStatus = MutableStateFlow("No Connection")
    val connectionStatus: StateFlow<String> = _connectionStatus
    
    init {
        viewModelScope.launch {
            context.observeConnectivity()
                .collectLatest { connectionType ->
                    _connectionStatus.emit(connectionType)
                }
        }
    }
}
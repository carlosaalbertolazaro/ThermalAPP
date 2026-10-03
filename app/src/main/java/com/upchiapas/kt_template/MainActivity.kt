package com.upchiapas.kt_template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.kt_template.ui.ThermalScreen
import com.upchiapas.kt_template.ui.ThermalViewModel
import com.upchiapas.kt_template.ui.theme.Kt_templateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kt_templateTheme {
                val viewModel: ThermalViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ThermalScreen(
                        uiState = uiState,
                        onTdpChange = viewModel::onTdpChange,
                        onAmbientChange = viewModel::onAmbientChange,
                        onOverclockToggle = viewModel::onOverclockToggle,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
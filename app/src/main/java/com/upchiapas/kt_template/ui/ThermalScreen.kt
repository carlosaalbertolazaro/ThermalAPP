package com.upchiapas.kt_template.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.upchiapas.kt_template.R

@Composable
fun ThermalScreen(
    uiState: ThermalUiState,
    onTdpChange: (Float) -> Unit,
    onAmbientChange: (Float) -> Unit,
    onOverclockToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.screen_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = statusColor(uiState.status),
                contentColor = Color.Black
        )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.temperature_format, uiState.finalTemperature),
                    style = MaterialTheme.typography.displayMedium
                )
                Text(text = stringResource(statusMessage(uiState.status)))
                Text(text = stringResource(R.string.loss_format, uiState.performanceLossPercent)
                )
            }

        }

        Text(text = stringResource(R.string.tdp_format, uiState.tdpWatts))
        Slider(
            value = uiState.tdpWatts,
            onValueChange = onTdpChange,
            valueRange = 65f..250f
            )
        Text(text = stringResource(R.string.ambient_format, uiState.ambientCelsius))
        Slider(
            value = uiState.ambientCelsius,
            onValueChange = onAmbientChange,
            valueRange = 15f..40f
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.overclock_label),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = uiState.isOverclockEnabled,
                onCheckedChange = onOverclockToggle
            )
        }

    }
}

private fun statusColor(status: ThermalStatus): Color = when (status){
    ThermalStatus.SAFE -> Color(0xFFC8E6C9)
    ThermalStatus.WARNING -> Color(0xFFFFF59D)
    ThermalStatus.DANGER -> Color(0xFFFFCDD2)
    }

@StringRes
private fun statusMessage(status: ThermalStatus): Int = when (status) {
    ThermalStatus.SAFE -> R.string.status_safe
    ThermalStatus.WARNING -> R.string.status_warning
    ThermalStatus.DANGER -> R.string.status_danger
}
@Preview(showBackground = true)
@Composable
private fun ThermalScreenPreview() {
    ThermalScreen(
        uiState = ThermalUiState(finalTemperature = 77.5, status = ThermalStatus.WARNING),
        onTdpChange = {},
        onAmbientChange = {},
        onOverclockToggle = {}
    )
}
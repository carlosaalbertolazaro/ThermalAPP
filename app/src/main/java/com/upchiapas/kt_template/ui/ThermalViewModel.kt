package com.upchiapas.kt_template.ui

import androidx.lifecycle.ViewModel
import com.upchiapas.kt_template.data.Cooler
import com.upchiapas.kt_template.data.ThermalPaste
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ThermalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(calculate(ThermalUiState()))
    val uiState: StateFlow<ThermalUiState> = _uiState.asStateFlow()

    fun onTdpChange(value: Float) {
        _uiState.update { calculate(it.copy(tdpWatts = value)) }
    }

    fun onAmbientChange(value: Float) {
        _uiState.update { calculate(it.copy(ambientCelsius = value)) }
    }

    fun onCoolerSelected(cooler: Cooler) {
        _uiState.update { calculate(it.copy(selectedCooler = cooler)) }
    }

    fun onPasteSelected(paste: ThermalPaste) {
        _uiState.update { calculate(it.copy(selectedPaste = paste)) }
    }

    fun onOverclockToggle(enabled: Boolean) {
        _uiState.update { calculate(it.copy(isOverclockEnabled = enabled)) }
    }

    private fun calculate(state: ThermalUiState): ThermalUiState {
        val power = if (state.isOverclockEnabled) {
            state.tdpWatts * OVERCLOCK_FACTOR
        } else {
            state.tdpWatts.toDouble()
        }
        val resistance = state.selectedCooler.thermalResistence +
                state.selectedPaste.thermalResistence
        val temperature = state.ambientCelsius + power * resistance

        val status = when {
            temperature > DANGER_CELSIUS -> ThermalStatus.DANGER
            temperature >= WARNING_CELSIUS -> ThermalStatus.WARNING
            else -> ThermalStatus.SAFE
        }
        val loss = if (temperature > DANGER_CELSIUS) {
            ((temperature - DANGER_CELSIUS) * LOSS_PER_DEGREE)
                .coerceIn(0.0, MAX_LOSS_PERCENT)
                .toInt()
        } else {
            0
        }

        return state.copy(
            effectivePowerWatts = power,
            totalResistance = resistance,
            finalTemperature = temperature,
            status = status,
            isThrottling = temperature > DANGER_CELSIUS,
            performanceLossPercent = loss
        )
    }

    private companion object {
        const val OVERCLOCK_FACTOR = 1.25
        const val WARNING_CELSIUS = 70.0
        const val DANGER_CELSIUS = 90.0
        const val LOSS_PER_DEGREE = 2.0
        const val MAX_LOSS_PERCENT = 50.0
    }
}
package com.upchiapas.kt_template.ui

import com.upchiapas.kt_template.data.Cooler
import com.upchiapas.kt_template.data.ThermalPaste
data class ThermalUiState(
    val tdpWatts : Float = 150f,
    val ambientCelsius : Float = 25f,
    val selecterdCooler : Cooler = Cooler.TOWER,
    val selectedThermalPaste : ThermalPaste = ThermalPaste.GENERIC,
    val isOverclockEnabled : Boolean = false,
    val effectivePowerWatts : Double = 0.0,
    val totalResistance : Double = 0.0,
    val thermalStatus : ThermalStatus = ThermalStatus.SAFE,
    val isThrottling : Boolean = false,
    val performanceLossPercent : Int = 0
)


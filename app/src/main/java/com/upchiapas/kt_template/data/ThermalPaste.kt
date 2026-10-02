package com.upchiapas.kt_template.data

import androidx.annotation.StringRes
import com.upchiapas.kt_template.R

enum class ThermalPaste (
    @StringRes val nameRes: Int,
            val thermalResistence: Double
) {
    GENERIC(R.string.paste_generic, 0.10),
    PTM(R.string.paste_ptm, 0.04),
    LIQUID_METAL(R.string.paste_liquid_metal, 0.01)
}
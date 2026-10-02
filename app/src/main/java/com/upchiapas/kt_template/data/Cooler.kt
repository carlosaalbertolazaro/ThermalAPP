package com.upchiapas.kt_template.data
import androidx.annotation.StringRes
import com.upchiapas.kt_template.R

enum class Cooler (
    @StringRes val nameRes: Int,
            val thermalResistence: Double

) {
    STOCK(R.string.cooler_stock, 0.45),
    TOWER(R.string.cooler_tower, 0.25),
    AIO_240(R.string.cooler_aio, 0.15)
}
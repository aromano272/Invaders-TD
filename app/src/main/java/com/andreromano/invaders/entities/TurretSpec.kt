package com.andreromano.invaders.entities

import android.graphics.Color

enum class TurretSpec(
    val shootDamage: Int,
    val shootDelay: Int,
    val cost: Int,
    val rangeRadiusToWidthFactor: Float
) {
    FAST(80, 200, 50, 2f),
    STRONG(300, 600, 100, 4f),
    SPREADER(100, 300, 150, 2f)
}

val TurretSpec.color: Int
    get() = when (this) {
        TurretSpec.FAST -> Color.BLUE
        TurretSpec.STRONG -> Color.RED
        TurretSpec.SPREADER -> Color.MAGENTA
    }

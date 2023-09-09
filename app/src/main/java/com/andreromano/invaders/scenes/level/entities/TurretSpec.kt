package com.andreromano.invaders.scenes.level.entities

import android.graphics.Color

enum class TurretSpec(
    val shootDamage: Int,
    val shootDelay: Int,
    val cost: Int,
    val rangeRadiusToWidthFactor: Float,
    val upgradeSpec: UpgradeSpec,
) {
    FAST(80, 200, 50, 2f, UpgradeSpec.FAST),
    STRONG(300, 600, 100, 4f, UpgradeSpec.STRONG),
    SPREADER(100, 300, 150, 2f, UpgradeSpec.SPREADER),
}

val TurretSpec.color: Int
    get() = when (this) {
        TurretSpec.FAST -> Color.BLUE
        TurretSpec.STRONG -> Color.RED
        TurretSpec.SPREADER -> Color.MAGENTA
    }

enum class UpgradeSpec(
    val shootDamageMultiplier: Float,
    val shootDelayMultiplier: Float,
    val upgradeCostMultiplier: Float,
    val rangeRadiusToWidthFactorMultiplier: Float,
    val additionalBullets: Int,
    val maxLevel: Int
) {
    FAST(1.5f, 0.5f, 2f, 1.05f, 0, 4),
    STRONG(2f, 0.8f, 2f, 1.1f, 0, 4),
    SPREADER(1.2f, 0.8f, 2f, 1.05f, 1, 4),
}

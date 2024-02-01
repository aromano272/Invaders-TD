package com.andreromano.invaders.scenes.level.entities

import android.graphics.Color
import java.lang.UnsupportedOperationException

enum class TowerSpec(
    val shootDamage: Int,
    val shootDelay: Int,
    val cost: Int,
    val rangeRadiusToWidthFactor: Float,
    val upgradeSpec: UpgradeSpec,
) {
    FAST(120, 600, 50, 2f, UpgradeSpec.FAST),
    STRONG(300, 1400, 100, 4f, UpgradeSpec.STRONG),
    SPREADER(150, 500, 150, 2f, UpgradeSpec.SPREADER),
}

val TowerSpec.color: Int
    get() = when (this) {
        TowerSpec.FAST -> Color.BLUE
        TowerSpec.STRONG -> Color.RED
        TowerSpec.SPREADER -> Color.MAGENTA
    }

enum class UpgradeSpec(
    val shootDamageMultiplier: Float,
    val shootDelayMultiplier: Float,
    val upgradeCostMultiplier: Float,
    val rangeRadiusToWidthFactorMultiplier: Float,
    val additionalBulletsFiredPerShotMultiplier: Int,
    val maxLevel: Int
) {
    FAST(1.75f, 0.75f, 2f, 1.05f, 0, 3),
    STRONG(2.25f, 0.8f, 2f, 1.1f, 0, 3),
    SPREADER(1.2f, 0.8f, 2f, 1.05f, 2, 3),
}

fun TowerSpec.totalMoneySpentForLevel(level: Int): Int {
    var result = cost.toFloat()
    repeat(level - 1) {
        val upgradeCostForLevel = upgradeCostForLevel(it + 1)
        result += upgradeCostForLevel
    }
    return result.toInt()
}
fun TowerSpec.shootDamageForLevel(level: Int): Int {
    var result = shootDamage.toFloat()
    repeat(level - 1) {
        result *= upgradeSpec.shootDamageMultiplier
    }
    return result.toInt()
}
fun TowerSpec.shootDelayForLevel(level: Int): Int {
    var result = shootDelay.toFloat()
    repeat(level - 1) {
        result *= upgradeSpec.shootDelayMultiplier
    }
    return result.toInt()
}
fun TowerSpec.upgradeCostForLevel(level: Int): Int {
    var result = cost.toFloat()
    repeat(level - 1) {
        result *= upgradeSpec.upgradeCostMultiplier
    }
    return result.toInt()
}
fun TowerSpec.rangeRadiusToWidthFactorForLevel(level: Int): Int {
    var result = rangeRadiusToWidthFactor.toFloat()
    repeat(level - 1) {
        result *= upgradeSpec.rangeRadiusToWidthFactorMultiplier
    }
    return result.toInt()
}
fun TowerSpec.bulletsFiredPerShotForLevel(level: Int): Int {
    if (this != TowerSpec.SPREADER) throw UnsupportedOperationException("Make sure the rest of the code handles this correct if we plan to use this on towers that are not SPREADER")
    var result = 1
    repeat(level - 1) {
        result *= upgradeSpec.additionalBulletsFiredPerShotMultiplier
    }
    return result
}

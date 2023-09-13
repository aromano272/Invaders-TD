package com.andreromano.invaders.scenes.level

import com.andreromano.invaders.Level
import com.andreromano.invaders.scenes.level.entities.TurretEntity
import com.andreromano.invaders.scenes.level.entities.TurretSpec
import java.io.Serializable

data class SaveableLevelState(
    val currentLevel: Level,
    val currentWave: Int,
    val currMoney: Int,
    val placedTurrets: List<SaveableTurret>,
) : Serializable {
    companion object {
        fun from(levelState: LevelState): SaveableLevelState = SaveableLevelState(
            currentLevel = levelState.currentLevel,
            currentWave = levelState.currentWave,
            currMoney = levelState.currMoney,
            placedTurrets = levelState.entitiesMap.flatten()
                .filterIsInstance<TurretEntity>()
                .map { turret ->
                    SaveableTurret(
                        tileX = turret.tileX,
                        tileY = turret.tileY,
                        spec = turret.spec,
                        currLevel = turret.currLevel,
                    )
                }
        )
    }
}

data class SaveableTurret(
    val tileX: Int,
    val tileY: Int,
    val spec: TurretSpec,
    val currLevel: Int,
) : Serializable

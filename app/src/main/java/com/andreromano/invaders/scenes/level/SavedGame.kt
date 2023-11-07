package com.andreromano.invaders.scenes.level

import com.andreromano.invaders.Level
import com.andreromano.invaders.scenes.level.entities.TowerEntity
import com.andreromano.invaders.scenes.level.entities.TowerSpec
import java.io.Serializable

data class SaveableLevelState(
    val currentLevel: Level,
    val currentWave: Int,
    val currMoney: Int,
    val placedTowers: List<SaveableTower>,
) : Serializable {
    companion object {
        fun from(levelState: LevelState): SaveableLevelState = SaveableLevelState(
            currentLevel = levelState.currentLevel,
            currentWave = levelState.currentWave,
            currMoney = levelState.currMoney,
            placedTowers = levelState.entitiesMap.flatten()
                .filterIsInstance<TowerEntity>()
                .map { tower ->
                    SaveableTower(
                        tileX = tower.tileX,
                        tileY = tower.tileY,
                        spec = tower.spec,
                        currLevel = tower.currLevel,
                    )
                }
        )
    }
}

data class SaveableTower(
    val tileX: Int,
    val tileY: Int,
    val spec: TowerSpec,
    val currLevel: Int,
) : Serializable

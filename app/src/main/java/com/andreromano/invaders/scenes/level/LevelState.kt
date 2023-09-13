package com.andreromano.invaders.scenes.level

import com.andreromano.invaders.Entity
import com.andreromano.invaders.Level
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.scenes.level.entities.BottomMenuEntity
import com.andreromano.invaders.scenes.level.entities.BulletEntity
import com.andreromano.invaders.scenes.level.entities.EnemyEntity
import com.andreromano.invaders.scenes.level.entities.GamePauseEntity
import com.andreromano.invaders.scenes.level.entities.GameSpeedEntity
import com.andreromano.invaders.scenes.level.entities.Waypoint
import java.io.Serializable

class LevelState(
    var currentLevel: Level
) {
    lateinit var currentLevelPath: List<Waypoint>
    var currentWave: Int = 0

    lateinit var entitiesMap: Array<Array<TiledEntity?>>

    var currMoney: Int = currentLevel.startingMoney

    var enemyEntities = mutableListOf<EnemyEntity>()
    var bulletEntities = mutableListOf<BulletEntity>()

    var selectedTileX: Int = -1
    var selectedTileY: Int = -1
    val selectedEntity: Entity?
        get() = try {
            entitiesMap[selectedTileY][selectedTileX]
        } catch (ex: ArrayIndexOutOfBoundsException) {
            null
        }

    var bottomMenuEntity: BottomMenuEntity? = null
    var gameSpeedEntity: GameSpeedEntity? = null
    var gamePauseEntity: GamePauseEntity? = null
}

private var _levelState: LevelState? = null
var levelState: LevelState
    get() = _levelState!!
    set(value) {
        _levelState = value
    }

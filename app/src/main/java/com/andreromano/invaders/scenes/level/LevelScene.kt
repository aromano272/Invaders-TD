package com.andreromano.invaders.scenes.level

import android.graphics.*

import com.andreromano.invaders.ClickListenerRegistry
import com.andreromano.invaders.Game
import com.andreromano.invaders.Level
import com.andreromano.invaders.Persistence
import com.andreromano.invaders.Scene
import com.andreromano.invaders.TerrainType
import com.andreromano.invaders.TileEdges
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.ViewEvent
import com.andreromano.invaders.dot
import com.andreromano.invaders.extensions.toPx
import com.andreromano.invaders.scenes.intro.IntroScene
import com.andreromano.invaders.scenes.level.entities.BottomMenuEntity
import com.andreromano.invaders.scenes.level.entities.BuildableEntity
import com.andreromano.invaders.scenes.level.entities.EndEntity
import com.andreromano.invaders.scenes.level.entities.EnemyEntity
import com.andreromano.invaders.scenes.level.entities.GamePauseEntity
import com.andreromano.invaders.scenes.level.entities.GameSpeedEntity
import com.andreromano.invaders.scenes.level.entities.PathEntity
import com.andreromano.invaders.scenes.level.entities.StartEntity
import com.andreromano.invaders.scenes.level.entities.TurretEntity
import com.andreromano.invaders.scenes.level.entities.WallEntity
import com.andreromano.invaders.scenes.level.entities.Waypoint
import java.lang.Exception

class LevelScene(
    game: Game,
    private var savedGameToLoad: SaveableLevelState?
) : Scene(game) {

    private var hasLoadedLevel = false
    private var screenWidth = game.width
    private var screenHeight = game.height
    private var boardSceneWidth = 0
    private var boardSceneHeight = 0
    private var entityWidth = 0
    private var entityHeight = 0
    private var mapTileWidth = 0
    private var mapTileHeight = 0

    private var currTime: Long = 0

    private var score: Int = 0

    private lateinit var startEntity: StartEntity
    private lateinit var endEntity: EndEntity


    private val paintSelected = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = 6f
    }

    private val redStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 4f
    }

    private val greenStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = 4f
    }

    private val greenPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val blackPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val bluePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    private val whitePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val yellowPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
    }

    private val debugTurretTextPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        typeface = Typeface.DEFAULT_BOLD
        color = Color.BLACK
    }

    private val boldTextPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }

    private val deferred: MutableList<() -> Unit> = mutableListOf()

    override fun updateAndRender(canvas: Canvas, deltaTime: Int) {
        if (!hasLoadedLevel) {
            val level = savedGameToLoad?.currentLevel ?: Level.ONE
            levelState = LevelState(level)
            loadLevel(level)
            hasLoadedLevel = true
            return
        }
        val deltaTime = (deltaTime * (levelState.gameSpeedEntity?.currMultiplier ?: 1f)).toInt()
        levelState.entitiesMap.forEach { rows ->
            rows.forEach { entity ->
                entity?.update(deltaTime)
                entity?.render(canvas)
            }
        }
        levelState.enemyEntities.forEach { entity ->
            entity.update(deltaTime)
            entity.render(canvas)
        }
        levelState.bulletEntities.forEach { entity ->
            entity.update(deltaTime)
            entity.render(canvas)
        }
        levelState.bottomMenuEntity?.update(deltaTime)
        levelState.bottomMenuEntity?.render(canvas)
        levelState.selectedEntity?.let {
            canvas.drawRect(it.hitbox, paintSelected)
        }
        levelState.gameSpeedEntity?.update(deltaTime)
        levelState.gameSpeedEntity?.render(canvas)
        levelState.gamePauseEntity?.update(deltaTime)
        levelState.gamePauseEntity?.render(canvas)

        updateGameState(deltaTime, canvas)

        clearDestroyedEntities()

//        canvas.drawText("Rect Pos: (${pacman?.x}, ${pacman?.y})", sceneWidth / 2f, sceneHeight / 2f, textPaint)
        canvas.drawText("Wave: ${levelState.currentWave + 1}, Enemies: ${levelState.enemyEntities.size}", 50f, boardSceneHeight - 50f, boldTextPaint)
        canvas.drawText("Money: ${levelState.currMoney}", 50f, boardSceneHeight - 50f + boldTextPaint.textSize, boldTextPaint)

        // clear padding
//        canvas.drawRect(Rect(boardSceneWidth, 0, screenWidth, screenHeight), blackPaint)
//        canvas.drawRect(Rect(0, boardSceneHeight, screenWidth, screenHeight), blackPaint)

        drawDebug(canvas)

        deferred.forEach {
            it()
        }
        deferred.clear()
    }

    private var spawnedCount = 0
    private var nextSpawnTime = 0L

    fun updateGameState(deltaTime: Int, canvas: Canvas) {
        spawnEnemyIfNeeded(deltaTime, canvas)
        spawnNextWaveIfNeeded(deltaTime)
        advanceLevelIfNeeded(deltaTime)

        currTime += deltaTime
    }

    private fun spawnEnemyIfNeeded(deltaTime: Int, canvas: Canvas) {
        val wave = levelState.currentLevel.waves[levelState.currentWave]
        if (spawnedCount == wave.enemyCount) return

        val newTime = currTime + deltaTime
        val timeDiff = (newTime - nextSpawnTime).toInt()
        if (timeDiff >= 0) {
            with (startEntity) {
                val enemy = EnemyEntity(pos, width, height,
                    wave.enemyHealth, wave.enemySpeed, wave.enemyMoney, levelState.currentLevelPath)
                levelState.enemyEntities += enemy
                enemy.update(timeDiff)
                enemy.render(canvas)

                spawnedCount++
                nextSpawnTime += wave.enemySpawnDelay
            }
        }
    }

    private fun spawnNextWaveIfNeeded(deltaTime: Int) {
        val wave = levelState.currentLevel.waves[levelState.currentWave]
        if (levelState.enemyEntities.isEmpty() && spawnedCount == wave.enemyCount) {
            levelState.currentWave++
            spawnedCount = 0
            nextSpawnTime = currTime
            Persistence.save(SaveableLevelState.from(levelState))
        }
    }

    private fun advanceLevelIfNeeded(deltaTime: Int) {
        if (levelState.currentWave == levelState.currentLevel.waves.size) {
            val nextLevel = Level.values().indexOf(levelState.currentLevel).let {
                if (it >= Level.values().size) return
                Level.values()[it + 1]
            }
            levelState.currentWave = 0
            spawnedCount = 0
            nextSpawnTime = currTime
            loadLevel(nextLevel)
        }
    }

    private fun clearDestroyedEntities() {
        val destroyedEnemies = levelState.enemyEntities.filter { it.destroyed }
        destroyedEnemies.forEach { it ->
            if (it.killed) levelState.currMoney += it.money
        }
        levelState.enemyEntities.removeAll(destroyedEnemies)
        levelState.bulletEntities.removeIf { it.destroyed }
    }


    fun defer(runnable: () -> Unit) {
        deferred += runnable
    }

    override fun onViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ScreenClicked -> {
                val tileX = getTileXFromScreenX(event.x.toInt())
                val tileY = getTileYFromScreenY(event.y.toInt())
                val entity = try {
                    levelState.entitiesMap[tileY][tileX]
                } catch (ex: Exception) {
                    null
                }

                if (entity is BuildableEntity || entity is TurretEntity) {
                    levelState.selectedTileX = tileX
                    levelState.selectedTileY = tileY
                }
                ClickListenerRegistry.onScreenClicked(event.x, event.y)
            }
        }
    }

    private fun removeEntity(entity: TiledEntity) {
        levelState.entitiesMap[entity.tileY][entity.tileX] = null
    }

    override fun sceneSizeChanged() {
        require(game.height > game.width) {
            "Game doesn't support landscape mode"
        }
        screenWidth = game.width
        screenHeight = game.height

        loadLevel(levelState.currentLevel)
    }

    fun restartLevel() {
        loadLevel(levelState.currentLevel)
    }

    fun loadLevel(level: Level) {
        levelState.currentLevel = level
        val rows = level.gameboard.split("\n")

        mapTileWidth = rows.first().length
        mapTileHeight = rows.size
        entityWidth = screenWidth / mapTileWidth
        entityHeight = entityWidth

        // Make size divisible by 2 so we don't issues with center
        entityWidth -= entityWidth % 2
        entityHeight -= entityHeight % 2

        // TODO: Pad and center gameboard
        val remainderWidth = screenWidth % mapTileWidth

        boardSceneWidth = screenWidth - remainderWidth
        boardSceneHeight = entityHeight * mapTileHeight

        levelState.entitiesMap = Array<Array<TiledEntity?>>(mapTileHeight) {
            Array(mapTileWidth) {
                null
            }
        }

        rows.forEachIndexed { tileY, row ->
            row.forEachIndexed { tileX, c ->
                val x = getScreenXFromTileX(tileX).toFloat()
                val y = getScreenYFromTileY(tileY).toFloat()
                val pos = Vec2F(x, y)
                val entity: TiledEntity = when (c) {
                    'o' -> {
                        StartEntity(pos, tileX, tileY, entityWidth, entityHeight).also {
                            startEntity = it
                        }
                    }
                    'x' -> {
                        EndEntity(pos, tileX, tileY, entityWidth, entityHeight).also {
                            endEntity = it
                        }
                    }
                    '#' -> {
                        WallEntity(pos, tileX, tileY, entityWidth, entityHeight)
                    }
                    '□' -> {
                        BuildableEntity(pos, tileX, tileY, entityWidth, entityHeight)
                    }
                    ' ' -> {
                        PathEntity(pos, tileX, tileY, entityWidth, entityHeight)
                    }
                    else -> throw UnsupportedOperationException("Could not parse symbol: '$c' at x: $x y: $y")
                }
                levelState.entitiesMap[tileY][tileX] = entity
            }
        }

        walkThroughLevelAndCreatePath()
        walkThroughLevelAndUpdateTerrainTileEdges()

        levelState.bottomMenuEntity = BottomMenuEntity(
            this,
            Vec2F(screenWidth / 2f, screenHeight - 200 / 2f),
             screenWidth, 200,
            spawnTurret = { turret ->
                levelState.currMoney -= turret.totalMoneySpent
                levelState.entitiesMap[turret.tileY][turret.tileX] = turret
            },
            spawnBullet = { bullet -> levelState.bulletEntities.add(bullet) }
        )
        val gameSpeedTileX = mapTileWidth - 2
        val gameSpeedTileY = 0
        val gamePauseTileX = gameSpeedTileX + 1
        val gamePauseTileY = 0
        levelState.gameSpeedEntity = GameSpeedEntity(
            this,
            Vec2F(
                x = getScreenXFromTileX(gameSpeedTileX).toFloat(),
                y = getScreenYFromTileY(gameSpeedTileY).toFloat(),
            ),
            entityWidth,
            entityHeight
        )
        levelState.gamePauseEntity = GamePauseEntity(
            this,
            Vec2F(
                x = getScreenXFromTileX(gamePauseTileX).toFloat(),
                y = getScreenYFromTileY(gamePauseTileY).toFloat(),
            ),
            entityWidth,
            entityHeight,
            onEntityClick = {
                game.changeScene(IntroScene(game))
            }
        )

        val savedGame = savedGameToLoad
        if (savedGame != null) {
            levelState.currentWave = savedGame.currentWave
            levelState.currMoney = savedGame.currMoney
            savedGame.placedTurrets.forEach { savedTurret ->
                val target = levelState.entitiesMap[savedTurret.tileY][savedTurret.tileX]
                if (target is BuildableEntity) {
                    val entity = TurretEntity(
                        pos = target.pos,
                        tileX = savedTurret.tileX,
                        tileY = savedTurret.tileY,
                        width = target.width,
                        height = target.height,
                        spec = savedTurret.spec,
                        spawnBullet = { bullet -> levelState.bulletEntities.add(bullet) },
                    )

                    entity.restoreUpgradeLevel(savedTurret.currLevel)

                    levelState.entitiesMap[savedTurret.tileY][savedTurret.tileX] = entity
                }
            }

            this.savedGameToLoad = null
        }
        Persistence.save(SaveableLevelState.from(levelState))
    }

    private fun walkThroughLevelAndCreatePath() {
        val transforms = listOf(
            0 to 1,
            -1 to 0,
            0 to -1,
            1 to 0
        )

        val pathEntities = mutableListOf<TiledEntity>(startEntity)

        var prevEntity: TiledEntity = startEntity
        var currentEntity: TiledEntity = startEntity
        while (currentEntity != endEntity) {
            val x = currentEntity.tileX
            val y = currentEntity.tileY

            val entity = transforms
                .mapNotNull { (tY, tX) -> levelState.entitiesMap[y + tY][x + tX] }
                .first { entity ->
                    entity != prevEntity && (entity is PathEntity || entity is EndEntity)
                }

            prevEntity = currentEntity
            currentEntity = entity
            pathEntities += entity
        }

        val waypoints = mutableListOf<Waypoint>()

        var currStartPos: Vec2F = pathEntities[0].pos
        var currEndPos: Vec2F = pathEntities[1].pos
        var currDirection = (currEndPos - currStartPos).normalized()
        pathEntities.drop(2).forEach { entity ->
            val newEndPos = entity.pos
            val newDirection = (newEndPos - currEndPos).normalized()
            val isSameDirection = dot(currDirection, newDirection) == 1f
            if (isSameDirection) {
                currEndPos = newEndPos
            } else {
                waypoints += Waypoint(currStartPos, currEndPos)
                currStartPos = currEndPos
                currEndPos = newEndPos
                currDirection = newDirection
            }
        }
        waypoints += Waypoint(currStartPos, currEndPos)

        levelState.currentLevelPath = waypoints
    }

    private fun walkThroughLevelAndUpdateTerrainTileEdges() {
        val terrain = levelState.entitiesMap
        terrain.forEachIndexed { y, rows ->
            rows.forEachIndexed { x, entity ->
                if (entity == null) return@forEachIndexed
                val top = terrain.getOrNull(y - 1, x)?.terrainType ?: TerrainType.GRASS
                val right = terrain.getOrNull(y, x + 1)?.terrainType ?: TerrainType.GRASS
                val bottom = terrain.getOrNull(y + 1, x)?.terrainType ?: TerrainType.GRASS
                val left = terrain.getOrNull(y, x - 1)?.terrainType ?: TerrainType.GRASS

                entity.tileEdges = TileEdges(top, right, bottom, left)
            }
        }
    }

    fun drawDebug(canvas: Canvas) {
        levelState.currentLevelPath.forEach { waypoint ->
            val rectWidth = 20f
            val startRect = RectF(
                waypoint.startPos.x - rectWidth / 2f,
                waypoint.startPos.y - rectWidth / 2f,
                waypoint.startPos.x + rectWidth / 2f,
                waypoint.startPos.y + rectWidth / 2f,
            )
            val endRect = RectF(
                waypoint.endPos.x - rectWidth / 2f,
                waypoint.endPos.y - rectWidth / 2f,
                waypoint.endPos.x + rectWidth / 2f,
                waypoint.endPos.y + rectWidth / 2f,
            )
            canvas.drawRect(startRect, yellowPaint)
            canvas.drawRect(endRect, yellowPaint)
        }

        (levelState.selectedEntity as? TurretEntity)?.let {
            listOf(
                "currShootDamage" to it.currShootDamage,
                "currShootDelay" to it.currShootDelay,
                "totalMoneySpent" to it.totalMoneySpent,
                "currRangeRadiusToWidthFactor" to it.currRangeRadiusToWidthFactor,
                "currLevel" to it.currLevel,
            ).forEachIndexed { index, (key, value) ->
                canvas.drawText("$key: $value", it.hitbox.left, it.hitbox.bottom + 40 + debugTurretTextPaint.textSize * index, debugTurretTextPaint)
            }
        }
    }

    private fun getScreenXFromTileX(tileX: Int): Int = tileX * entityWidth + entityWidth / 2
    private fun getScreenYFromTileY(tileY: Int): Int = tileY * entityHeight + entityHeight / 2

    private fun getTileXFromScreenX(screenX: Int): Int = screenX / entityWidth
    private fun getTileYFromScreenY(screenY: Int): Int = screenY / entityHeight

}


fun Canvas.drawDebugVec(
    origin: Vec2F,
    vec: Vec2F,
    length: Float = 100f,
    color: Int = Color.MAGENTA,
) {
    val vec = vec * length
    drawLine(origin.x, origin.y,
        origin.x + vec.x, origin.y + vec.y,
        Paint().apply {
            style = Paint.Style.FILL
            this.color = color
            strokeWidth = 4f
        })
}

fun Canvas.drawDebugRect(
    center: Vec2F,
    width: Float,
    color: Int = Color.YELLOW
) {
    val rect = RectF(
        center.x - (width / 2f),
        center.y - (width / 2f),
        center.x + (width / 2f),
        center.y + (width / 2f),
    )
    drawRect(
        rect,
        Paint().apply {
            style = Paint.Style.FILL
            this.color = color
        }
    )
}

fun <T> Array<Array<T>>.getOrNull(y: Int, x: Int): T? = try {
    this[y][x]
} catch (ex: Exception) {
    null
}

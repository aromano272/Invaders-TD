package com.andreromano.invaders

import android.graphics.*
import com.andreromano.invaders.entities.*
import com.andreromano.invaders.extensions.toPx
import java.lang.Exception

object GameState {
    var currentLevelIndex: Int = 0
    var currentLevel: Level = Level.ONE
    lateinit var currentLevelPath: List<PathSegment>

    lateinit var entitiesMap: Array<Array<Entity?>>

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
}

class Game {

    private var screenWidth = 0
    private var screenHeight = 0
    private var boardSceneWidth = 0
    private var boardSceneHeight = 0
    private var entityWidth = 0
    private var entityHeight = 0
    private var mapTileWidth = 0
    private var mapTileHeight = 0

    private var frameCount: Long = 0
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

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        color = Color.WHITE
    }

    private val boldTextPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }

    private val deferred: MutableList<() -> Unit> = mutableListOf()

    fun updateAndRender(canvas: Canvas, deltaTime: Int) {
        currTime += deltaTime
        GameState.entitiesMap.forEach { rows ->
            rows.forEach { entity ->
                entity?.update(deltaTime)
                entity?.render(canvas)
            }
        }
        GameState.enemyEntities.forEach { entity ->
            entity.update(deltaTime)
            entity.render(canvas)
        }
        GameState.bulletEntities.forEach { entity ->
            entity.update(deltaTime)
            entity.render(canvas)
        }
        GameState.bottomMenuEntity?.update(deltaTime)
        GameState.bottomMenuEntity?.render(canvas)
        GameState.selectedEntity?.let {
            canvas.drawRect(it.hitbox, paintSelected)
        }

        updateGameState(deltaTime)

        clearDestroyedEntities()

//        canvas.drawText("Rect Pos: (${pacman?.x}, ${pacman?.y})", sceneWidth / 2f, sceneHeight / 2f, textPaint)
        canvas.drawText("Wave: ${currentWave + 1}, Enemies: ${GameState.enemyEntities.size}", 50f, boardSceneHeight - 50f, boldTextPaint)
        canvas.drawText("Money: ${GameState.currMoney}", 50f, boardSceneHeight - 50f + boldTextPaint.textSize, boldTextPaint)

        // clear padding
//        canvas.drawRect(Rect(boardSceneWidth, 0, screenWidth, screenHeight), blackPaint)
//        canvas.drawRect(Rect(0, boardSceneHeight, screenWidth, screenHeight), blackPaint)

        drawDebug(canvas)

        deferred.forEach {
            it()
        }
        deferred.clear()

        frameCount++
    }


    private var currentWave: Int = 0

    private var spawnedCount = 0
    private var nextSpawnTime = 0L

    fun updateGameState(deltaTime: Int) {
        spawnEnemyIfNeeded(deltaTime)
        spawnNextWaveIfNeeded(deltaTime)
        advanceLevelIfNeeded(deltaTime)

        currTime += deltaTime
    }

    private fun spawnEnemyIfNeeded(deltaTime: Int) {
        val wave = GameState.currentLevel.waves[currentWave]
        if (spawnedCount == wave.enemyCount) return

        val newTime = currTime + deltaTime
        if (newTime > nextSpawnTime) {
            with (startEntity) {
                val enemy = EnemyEntity(pos, tileX, tileY, width, height,
                    wave.enemyHealth, wave.enemySpeed, wave.enemyMoney, GameState.currentLevelPath)
                GameState.enemyEntities += enemy

                spawnedCount++
                nextSpawnTime += wave.enemySpawnDelay
            }
        }
    }

    private fun spawnNextWaveIfNeeded(deltaTime: Int) {
        val wave = GameState.currentLevel.waves[currentWave]
        if (GameState.enemyEntities.isEmpty() && spawnedCount == wave.enemyCount) {
            currentWave++
            spawnedCount = 0
            nextSpawnTime = currTime
        }
    }

    private fun advanceLevelIfNeeded(deltaTime: Int) {
        if (currentWave == GameState.currentLevel.waves.size) {
            GameState.currentLevelIndex++
            currentWave = 0
            spawnedCount = 0
            nextSpawnTime = currTime
            loadLevel(Level.values()[GameState.currentLevelIndex])
        }
    }

    private fun clearDestroyedEntities() {
        val destroyedEnemies = GameState.enemyEntities.filter { it.destroyed }
        destroyedEnemies.forEach { it ->
            if (it.killed) GameState.currMoney += it.money
        }
        GameState.enemyEntities.removeAll(destroyedEnemies)
        GameState.bulletEntities.removeIf { it.destroyed }
    }


    fun defer(runnable: () -> Unit) {
        deferred += runnable
    }

    fun onViewEvent(viewEvent: ViewEvent) {
        when (viewEvent) {
            is ViewEvent.ScreenClicked -> {
                val tileX = getTileXFromScreenX(viewEvent.x.toInt())
                val tileY = getTileYFromScreenY(viewEvent.y.toInt())
                val entity = try {
                    GameState.entitiesMap[tileY][tileX]
                } catch (ex: Exception) {
                    null
                }

                if (entity is BuildableEntity || entity is TurretEntity) {
                    GameState.selectedTileX = tileX
                    GameState.selectedTileY = tileY
                }
                ClickListenerRegistry.onScreenClicked(viewEvent.x, viewEvent.y)
            }
        }
    }

    private fun removeEntity(entity: Entity) {
        GameState.entitiesMap[entity.tileY][entity.tileX] = null
    }

    fun sceneSizeChanged(w: Int, h: Int) {
        require(h > w) {
            "Game doesn't support landscape mode"
        }
        screenWidth = w
        screenHeight = h

        loadLevel(GameState.currentLevel)
    }

    fun restartLevel() {
        loadLevel(GameState.currentLevel)
    }

    fun loadLevel(level: Level) {
        score = 0
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

        GameState.entitiesMap = Array<Array<Entity?>>(mapTileHeight) {
            Array(mapTileWidth) {
                null
            }
        }

        rows.forEachIndexed { tileY, row ->
            row.forEachIndexed { tileX, c ->
                val x = getScreenXFromTileX(tileX).toFloat()
                val y = getScreenYFromTileY(tileY).toFloat()
                val pos = Vec2F(x, y)
                val entity: Entity = when (c) {
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
                GameState.entitiesMap[tileY][tileX] = entity
            }
        }

        walkThroughLevelAndCreatePath()

        GameState.bottomMenuEntity = BottomMenuEntity(
            Vec2F(screenWidth / 2f, screenHeight - 200 / 2f),
            0, 0, screenWidth, 200,
            spawnTurret = { turret ->
                GameState.currMoney -= turret.spec.cost
                GameState.entitiesMap[turret.tileY][turret.tileX] = turret
            },
            spawnBullet = { bullet -> GameState.bulletEntities.add(bullet) }
        )
    }

    private fun walkThroughLevelAndCreatePath() {
        val transforms = listOf(
            0 to 1,
            -1 to 0,
            0 to -1,
            1 to 0
        )

        val pathEntities = mutableListOf<Entity>(startEntity)

        var prevEntity: Entity = startEntity
        var currentEntity: Entity = startEntity
        while (currentEntity != endEntity) {
            val x = currentEntity.tileX
            val y = currentEntity.tileY

            val entity = transforms
                .mapNotNull { (tY, tX) -> GameState.entitiesMap[y + tY][x + tX] }
                .first { entity ->
                    entity != prevEntity && (entity is PathEntity || entity is EndEntity)
                }

            prevEntity = currentEntity
            currentEntity = entity
            pathEntities += entity
        }

        var prevPosition: Position? = null
        val pathSegments = mutableListOf<PathSegment>()

        pathEntities.forEach { entity ->
            val currPosition = entity.currentPos()
            val startPosition = prevPosition
            if (startPosition != null) {
                pathSegments += PathSegment(startPosition, currPosition)
            }
            prevPosition = currPosition
        }

        GameState.currentLevelPath = pathSegments
    }

    fun drawDebug(canvas: Canvas) {
    }

    sealed class ViewEvent {
        class ScreenClicked(val x: Float, val y: Float) : ViewEvent()
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

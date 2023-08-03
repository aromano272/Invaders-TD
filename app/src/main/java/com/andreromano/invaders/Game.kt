package com.andreromano.invaders

import android.graphics.*
import com.andreromano.invaders.entities.*
import com.andreromano.invaders.extensions.exhaustive
import com.andreromano.invaders.extensions.toPx

class Game {
    private var currentLevelIndex: Int = 0
    private var currentLevel: Level = Level.ONE
    private lateinit var currentLevelPath: List<PathSegment>

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


    private var entitiesMap: Array<Array<Entity?>> = Array<Array<Entity?>>(mapTileHeight) {
        Array(mapTileWidth) {
            null
        }
    }

    private var enemyEntities = mutableListOf<EnemyEntity>()
    private var bulletEntities = mutableListOf<BulletEntity>()

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
        entitiesMap.forEach { rows ->
            rows.forEach { entity ->
                entity?.update(deltaTime)
                entity?.render(canvas)
            }
        }
        enemyEntities.forEach { entity ->
            entity.update(deltaTime)
            entity.render(canvas)
        }
        bulletEntities.forEach { entity ->
            entity.update(deltaTime)
            entity.render(canvas)
        }

        updateGameState(deltaTime)

        clearDestroyedEntities()

//        canvas.drawText("Rect Pos: (${pacman?.x}, ${pacman?.y})", sceneWidth / 2f, sceneHeight / 2f, textPaint)
        canvas.drawText("Wave: ${currentWave + 1}, Enemies: ${enemyEntities.size}", 50f, boardSceneHeight - 50f, boldTextPaint)

        // clear padding
        canvas.drawRect(Rect(boardSceneWidth, 0, screenWidth, screenHeight), blackPaint)
        canvas.drawRect(Rect(0, boardSceneHeight, screenWidth, screenHeight), blackPaint)

        drawDebug(canvas)

        drawTurretMenu(canvas)

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
        val wave = currentLevel.waves[currentWave]
        if (spawnedCount == wave.enemyCount) return

        val newTime = currTime + deltaTime
        if (newTime > nextSpawnTime) {
            with (startEntity) {
                val enemy = EnemyEntity(pos, tileX, tileY, width, height,
                    wave.enemyHealth, wave.enemySpeed, wave.enemyMoney, currentLevelPath)
                enemyEntities += enemy

                spawnedCount++
                nextSpawnTime += wave.enemySpawnDelay
            }
        }
    }

    private fun spawnNextWaveIfNeeded(deltaTime: Int) {
        if (enemyEntities.isEmpty()) {
            currentWave++
            spawnedCount = 0
            nextSpawnTime = currTime
        }
    }

    private fun advanceLevelIfNeeded(deltaTime: Int) {
        if (currentWave == currentLevel.waves.size) {
            currentLevelIndex++
            currentWave = 0
            spawnedCount = 0
            nextSpawnTime = currTime
            loadLevel(Level.values()[currentLevelIndex])
        }
    }

    private fun clearDestroyedEntities() {
        enemyEntities.removeIf { it.destroyed }
        bulletEntities.removeIf { it.destroyed }
    }


    fun defer(runnable: () -> Unit) {
        deferred += runnable
    }

    fun onViewEvent(viewEvent: ViewEvent) {
        when (viewEvent) {
            ViewEvent.RESTART_CLICKED -> restartLevel()
        }.exhaustive
    }

    private fun removeEntity(entity: Entity) {
        entitiesMap[entity.tileY][entity.tileX] = null
    }

    fun sceneSizeChanged(w: Int, h: Int) {
        require(h > w) {
            "Game doesn't support landscape mode"
        }
        screenWidth = w
        screenHeight = h

        currentLevel?.let { loadLevel(it) }
    }

    fun restartLevel() {
        currentLevel?.let { loadLevel(it) }
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

        entitiesMap = Array<Array<Entity?>>(mapTileHeight) {
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
                    't' -> {
                        TurretEntity(pos, tileX, tileY,
                            entityWidth, entityHeight,
                            250,
                            { enemyEntities },
                            { bulletEntity -> bulletEntities.add(bulletEntity) }
                        )
                    }
                    else -> throw UnsupportedOperationException("Could not parse symbol: '$c' at x: $x y: $y")
                }
                entitiesMap[tileY][tileX] = entity
            }
        }

        walkThroughLevelAndCreatePath()
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
                .mapNotNull { (tY, tX) -> entitiesMap[y + tY][x + tX] }
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

        currentLevelPath = pathSegments
    }

    fun drawTurretMenu(canvas: Canvas) {

    }

    fun drawDebug(canvas: Canvas) {
        // assignemtn #3
//        val origin = Vec2F(screenWidth / 2f, sceneHeight / 2f)
//
//        val point = Vec2F(100f, 20f)
//
//        val xAxis = Vec2F(3f, 2f)
//        val yAxis = Vec2F(2f, -3f)
//
//        canvas.drawDebugVec(origin, xAxis, color = Color.RED)
//        canvas.drawDebugVec(origin, yAxis, color = Color.GREEN)
//        canvas.drawPoint(origin.x, origin.y, Paint().apply { style = Paint.Style.FILL; color = Color.WHITE; strokeWidth = 20f })
//
//        canvas.drawPoint(point.x, point.y, Paint().apply { style = Paint.Style.FILL; color = Color.MAGENTA; strokeWidth = 10f })
//
//        return
/*
        // assingment #2

        val origin = Vec2F(screenWidth / 2f, sceneHeight / 2f)

        canvas.drawLine(origin.x, 0f,
            origin.x, screenHeight.toFloat(),
            Paint().apply {
                style = Paint.Style.FILL
                color = Color.GREEN
                strokeWidth = 2f
            })

        canvas.drawLine(0f, origin.y,
            screenWidth.toFloat(), origin.y,
            Paint().apply {
                style = Paint.Style.FILL
                color = Color.RED
                strokeWidth = 2f
            })


        val enemy = enemyEntities.firstOrNull() ?: return

        val lookingAt = Vec2F(0f, -1f)
        val lookAtTrigger = 0.9
        var triggered = false

        val originToEnemy = enemy.pos - origin
        canvas.drawDebugVec(origin, originToEnemy, length = 1f, color = Color.YELLOW)
        val dotP = dot(lookingAt, originToEnemy.normalized())
        if (dotP >= lookAtTrigger) {
            triggered = true
        }
        println(dotP)


        val lookAtColor = if (triggered) Color.CYAN else Color.MAGENTA

        canvas.drawDebugVec(enemy.pos, lookingAt, color = lookAtColor)
*/

    }

    enum class ViewEvent {
        RESTART_CLICKED,
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

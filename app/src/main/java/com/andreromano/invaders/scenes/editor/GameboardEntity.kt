package com.andreromano.invaders.scenes.editor

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Scene
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.Vec2F

class GameboardEntity(
    private val scene: Scene,
    pos: Vec2F,
    width: Int,
    height: Int,
) : Entity(
    pos = pos,
    width = width,
    height = height,
) {

    private val paintSelected = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = 6f
    }

    var tileWidth = 1
        set(value) {
            field = value.coerceAtLeast(1)
        }
    var tileHeight = 1
        set(value) {
            field = value.coerceAtLeast(1)
        }

    var entitiesMap = Array<Array<TiledEntity?>>(tileHeight) {
        Array(tileWidth) {
            null
        }
    }

    var entityWidth = 0
    var entityHeight = 0

    private var remainderWidth = 0f

    private var boardSceneWidth = 0
    private var boardSceneHeight = 0

    var selectedTileX: Int? = null
    var selectedTileY: Int? = null

    fun invalidate() {
        entityWidth = width / tileWidth
        entityHeight = entityWidth

        // Make size divisible by 2 so we don't issues with center
        entityWidth -= entityWidth % 2
        entityHeight -= entityHeight % 2

        // TODO: Pad and center gameboard
        val remainderWidth = width % tileWidth

        boardSceneWidth = width - remainderWidth
        boardSceneHeight = entityHeight * tileHeight

        entitiesMap = entitiesMap.copyOf(tileHeight).map {
            it?.copyOf(tileWidth) ?: Array<TiledEntity?>(tileWidth) {
                null
            }
        }.toTypedArray().also {
            it.forEachIndexed { tileY, row ->
                row.forEachIndexed { tileX, entity ->
                    if (entity == null) return@forEachIndexed
                    val x = getScreenXFromTileX(tileX).toFloat()
                    val y = getScreenYFromTileY(tileY).toFloat()
                    val pos = Vec2F(x, y)
                    entity.pos = pos
                    entity.width = entityWidth
                    entity.height = entityHeight
                }
            }
        }
    }

    fun getScreenXFromTileX(tileX: Int): Int = tileX * entityWidth + entityWidth / 2
    fun getScreenYFromTileY(tileY: Int): Int = tileY * entityHeight + entityHeight / 2

    fun getTileXFromScreenX(screenX: Int): Int = screenX / entityWidth
    fun getTileYFromScreenY(screenY: Int): Int = screenY / entityHeight

    override fun update(deltaTime: Int) {
        entitiesMap.forEach { rows ->
            rows.forEach { entity ->
                entity?.update(deltaTime)
            }
        }
    }

    override fun render(canvas: Canvas) {
        entitiesMap.forEach { rows ->
            rows.forEach { entity ->
                entity?.render(canvas)
            }
        }
        val selectedTileX = selectedTileX
        val selectedTileY = selectedTileY
        val selectedEntity = if (selectedTileX != null && selectedTileY != null) {
            entitiesMap[selectedTileY][selectedTileX]
        } else {
            null
        }
        if (selectedEntity != null) {
            canvas.drawRect(selectedEntity.hitbox, paintSelected)
        }
    }

}
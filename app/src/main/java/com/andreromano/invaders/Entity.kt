package com.andreromano.invaders

import android.graphics.Canvas
import android.graphics.Rect
import kotlin.math.roundToInt

abstract class Entity(
    var pos: Vec2F,
    var tileX: Int,
    var tileY: Int,
    val width: Int,
    val height: Int,
) {
    private val _rect = Rect()
    val hitbox: Rect
        get() = _rect.apply {
            left = (pos.x - width / 2).roundToInt()
            top = (pos.y - height / 2).roundToInt()
            right = (pos.x + width / 2).roundToInt()
            bottom = (pos.y + height / 2).roundToInt()
        }

    fun currentPos(): Position = Position(pos, tileX, tileY)

    fun updatePos(pos: Position) {
        this.pos = pos.pos
        tileX = pos.tileX
        tileY = pos.tileY
    }

    abstract fun update(deltaTime: Int)
    abstract fun render(canvas: Canvas)

}

data class Position(
    val pos: Vec2F,
    val tileX: Int,
    val tileY: Int
)
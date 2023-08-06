package com.andreromano.invaders

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.roundToInt

abstract class Entity(
    var pos: Vec2F,
    var tileX: Int,
    var tileY: Int,
    val width: Int,
    val height: Int,
) {
    private val _rect = RectF()
    val hitbox: RectF
        get() = _rect.apply {
            left = (pos.x - width / 2)
            top = (pos.y - height / 2)
            right = (pos.x + width / 2)
            bottom = (pos.y + height / 2)
        }

    fun currentPos(): Position = Position(pos, tileX, tileY)

    fun updatePos(pos: Position) {
        this.pos = pos.pos
        tileX = pos.tileX
        tileY = pos.tileY
    }

    abstract fun update(deltaTime: Int)
    abstract fun render(canvas: Canvas)

    protected fun onClick(callback: () -> Boolean) {
        ClickListenerRegistry.register(this, callback)
    }

}

data class Position(
    val pos: Vec2F,
    val tileX: Int,
    val tileY: Int
)
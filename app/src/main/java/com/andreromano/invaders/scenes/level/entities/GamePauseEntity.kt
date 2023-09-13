package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.Vec2F
import kotlin.math.sqrt

class GamePauseEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
) : TiledEntity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height,
) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        this.color = Color.WHITE
    }

    private val triangleSideLength = width / 2.5f
    private val triangleWidth = 0.5f * sqrt(3f) * triangleSideLength
    private val trianglePath = trianglePath(0f)

    private val pauseBarTop = (height - triangleSideLength) / 2f
    private val pauseBarWidth = width / 5f
    private val pauseRects = arrayOf(
        RectF(
            hitbox.left + pauseBarWidth,
            hitbox.top + height / 4f,
            hitbox.right - pauseBarWidth * 2f,
            hitbox.bottom - height - height / 4f
        ),
        RectF(
            hitbox.left + pauseBarWidth * 3f,
            hitbox.top + height / 4f,
            hitbox.right - pauseBarWidth * 4f,
            hitbox.bottom - height - height / 4f
        ),
    )

    private fun trianglePath(xOffset: Float) = Path().apply {
        val origin = Vec2F(pos.x - (triangleWidth / 2f) + xOffset, pos.y - (triangleWidth / 2f))
        moveTo(origin.x, origin.y)
        lineTo(origin.x + triangleWidth, origin.y + triangleSideLength / 2f)
        lineTo(origin.x, origin.y + triangleSideLength)
        lineTo(origin.x, origin.y)
    }

    var isPaused: Boolean = false

    override fun update(deltaTime: Int) {
    }

    override fun render(canvas: Canvas) {
        if (isPaused) {
            canvas.drawPath(trianglePath, paint)
        } else {
            pauseRects.forEach {
                canvas.drawRect(it, paint)
            }
        }
    }
}
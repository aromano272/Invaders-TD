package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.andreromano.invaders.Scene
import com.andreromano.invaders.UiEntity
import com.andreromano.invaders.Vec2F
import kotlin.math.sqrt

class GamePauseEntity(
    scene: Scene,
    pos: Vec2F,
    width: Int,
    height: Int,
    private val onEntityClick: () -> Unit,
) : UiEntity(
    scene = scene,
    pos = pos,
    width = width,
    height = height,
) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        this.color = Color.WHITE
    }

    private val triangleSideLength = width / 2.5f
    private val triangleWidth = 0.5f * sqrt(3f) * triangleSideLength
    private val trianglePath = trianglePath()

    private val pauseBarWidth = width / 8f
    private val pauseRects = arrayOf(
        RectF(
            hitbox.left + pauseBarWidth * 2.5f,
            hitbox.top + height / 3.5f,
            hitbox.left + pauseBarWidth * 3.5f,
            hitbox.bottom - height / 3.5f
        ),
        RectF(
            hitbox.left + pauseBarWidth * 4.5f,
            hitbox.top + height / 3.5f,
            hitbox.left + pauseBarWidth * 5.5f,
            hitbox.bottom - height / 3.5f
        ),
    )

    private fun trianglePath() = Path().apply {
        val origin = Vec2F(pos.x - (triangleWidth / 2f), pos.y - (triangleWidth / 2f))
        moveTo(origin.x, origin.y)
        lineTo(origin.x + triangleWidth, origin.y + triangleSideLength / 2f)
        lineTo(origin.x, origin.y + triangleSideLength)
        lineTo(origin.x, origin.y)
    }

    override fun onClick(x: Float, y: Float): Boolean {
        onEntityClick()
        return true
    }

    var isPaused: Boolean = false

    override fun update(deltaTime: Int) {
        super.update(deltaTime)
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
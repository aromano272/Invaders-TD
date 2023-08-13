package com.andreromano.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Vec2F
import kotlin.math.sqrt

class GameSpeedEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height
) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        this.color = Color.WHITE
    }

    private val triangleSideLength = width / 2.5f
    private val triangleWidth = 0.5f * sqrt(3f) * triangleSideLength
    private val trianglePathsPerCurrStep = mapOf(
        0 to listOf(trianglePath(0f)),
        1 to listOf(trianglePath(-triangleWidth / 3f), trianglePath(triangleWidth / 3f)),
        2 to listOf(trianglePath(-triangleWidth / 1.5f), trianglePath(0f), trianglePath(triangleWidth / 1.5f)),
    )

    private fun trianglePath(xOffset: Float) = Path().apply {
        val origin = Vec2F(pos.x - (triangleWidth / 2f) + xOffset, pos.y - (triangleWidth / 2f))
        moveTo(origin.x, origin.y)
        lineTo(origin.x + triangleWidth, origin.y + triangleSideLength / 2f)
        lineTo(origin.x, origin.y + triangleSideLength)
        lineTo(origin.x, origin.y)
    }


    val currMultiplier: Float
        get() = steps[currStep]

    private var currStep = 0
    private var steps = arrayOf(1f, 2f, 4f)

    init {
        onClick {
            currStep = (currStep + 1) % steps.size
            true
        }
    }

    override fun update(deltaTime: Int) {
    }

    override fun render(canvas: Canvas) {
        val trianglePaths = trianglePathsPerCurrStep[currStep]!!
        trianglePaths.forEach { path ->
            canvas.drawPath(path, paint)
        }
    }
}
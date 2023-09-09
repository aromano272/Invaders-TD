package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Vec2F

class EndEntity(
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
    height = height,
) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    override fun update(deltaTime: Int) {
    }

    override fun render(canvas: Canvas) {
        canvas.drawRect(hitbox, paint)
    }
}
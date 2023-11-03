package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.Entity
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawTile
import kotlin.random.Random

class WallEntity(
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
    private val randomWallType = Random.nextInt(8)

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#964B00")
    }

    override fun update(deltaTime: Int) {
    }

    override fun render(canvas: Canvas) {
        canvas.drawTile(14, 5, hitbox)
        canvas.drawTile(15 + randomWallType, 5, hitbox)
    }
}
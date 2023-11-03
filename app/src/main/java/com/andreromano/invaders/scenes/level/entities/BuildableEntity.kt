package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.Entity
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawBuildableTile
import com.andreromano.invaders.drawTile
import com.andreromano.invaders.extensions.scale

class BuildableEntity(
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
        color = Color.parseColor("#FFF4BD")
    }

    private val paint2 = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#964B00")
    }

    var nineSliceIndex: Int = -1 // [0, 8]

    override fun update(deltaTime: Int) {
    }

    override fun render(canvas: Canvas) {
        canvas.drawBuildableTile(nineSliceIndex, hitbox)
    }
}
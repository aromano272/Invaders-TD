package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.times
import com.andreromano.invaders.Entity
import com.andreromano.invaders.TerrainType
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawTerrainTile
import com.andreromano.invaders.extensions.scale

class StartEntity(
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
    terrainType = TerrainType.DIRT
) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    override fun update(deltaTime: Int) {
    }

    override fun render(canvas: Canvas) {
        canvas.drawTerrainTile(this, hitbox)
        canvas.drawRect(hitbox.scale(0.3f), paint)
    }
}
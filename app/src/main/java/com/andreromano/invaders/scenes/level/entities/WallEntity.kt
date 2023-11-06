package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.Entity
import com.andreromano.invaders.TerrainType
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawObstacleTile
import com.andreromano.invaders.drawTerrainTile
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
    terrainType = TerrainType.GRASS
) {
    private val randomWallType = Random.nextInt()

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#964B00")
    }

    override fun update(deltaTime: Int) {
    }

    override fun render(canvas: Canvas) {
        canvas.drawTerrainTile(this, hitbox)
        canvas.drawObstacleTile(randomWallType, hitbox)
    }
}
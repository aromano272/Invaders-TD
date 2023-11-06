package com.andreromano.invaders

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

data class AnimationSpec(
    val bitmap: Bitmap,
    val tileSize: Int,
    val numColTiles: Int,
    val durationMs: Int,
)

class AnimatedEntity(
    pos: Vec2F,
    val spec: AnimationSpec,
) : Entity(
    pos = pos,
    width = spec.tileSize,
    height = spec.tileSize,
    posMode = PosMode.CENTER,
) {
    private val delayBetweenFrames = spec.durationMs / spec.numColTiles

    private var currTime = 0L
    private var nextFrameTime = 0L

    var currTileCol = 0

    var rotationDeg = 0

    override fun update(deltaTime: Int) {
        val newTime = currTime + deltaTime
        val timeDiff = newTime - nextFrameTime
        if (timeDiff >= 0) {
            currTileCol = (currTileCol + 1) % spec.tileSize
            nextFrameTime += delayBetweenFrames
        }
        currTime = newTime
    }

    override fun render(canvas: Canvas) {
        canvas.drawAnimationTile(this, hitbox)
    }
}
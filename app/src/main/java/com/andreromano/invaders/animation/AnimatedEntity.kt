package com.andreromano.invaders.animation

import android.graphics.Canvas
import com.andreromano.invaders.Entity
import com.andreromano.invaders.PosMode
import com.andreromano.invaders.TileAtlas
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawAnimationTile

open class AnimatedEntity(
    pos: Vec2F,
    val spec: AnimationSpec,
    val scale: Float = 1f
) : Entity(
    pos = pos,
    width = spec.tileSize,
    height = spec.tileSize,
    posMode = PosMode.CENTER,
) {
    private val delayBetweenFrames = spec.durationMs / spec.bitmaps.size

    private var currTime = 0L
    private var nextFrameTime = delayBetweenFrames

    var currTileCol = 0

    var rotationTetha = 0f

    override fun update(deltaTime: Int) {
        val newTime = currTime + deltaTime
        val timeDiff = newTime - nextFrameTime
        if (timeDiff >= 0) {
            currTileCol = (currTileCol + 1) % spec.bitmaps.size
            nextFrameTime += delayBetweenFrames
        }
        currTime = newTime
    }

    override fun render(canvas: Canvas) {
        canvas.drawAnimationTile(this, hitbox, scale)
    }
}
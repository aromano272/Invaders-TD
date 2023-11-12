package com.andreromano.invaders.animation

import android.graphics.Canvas
import com.andreromano.invaders.Entity
import com.andreromano.invaders.PosMode
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawAnimationTile

open class AnimatedEntity(
    pos: Vec2F,
    val spec: AnimationSpec,
    val durationMs: Int,
    val scale: Float = 1f,
) : Entity(
    pos = pos,
    width = spec.tileSize,
    height = spec.tileSize,
    posMode = PosMode.CENTER,
) {
    var isRunning = false
        private set

    private val delayBetweenFrames = durationMs / spec.bitmaps.size

    private var currTime = 0L
    private var nextFrameTime = delayBetweenFrames

    var currFrame = 0

    var rotationTetha = 0f

    fun start() {
        isRunning = true
    }

    fun stop() {
        isRunning = false
        currTime = 0
        nextFrameTime = delayBetweenFrames
        currFrame = 0
    }

    override fun update(deltaTime: Int) {
        if (!isRunning) return
        val newTime = currTime + deltaTime
        val timeDiff = newTime - nextFrameTime
        if (timeDiff >= 0) {
            val newFrame = (currFrame + 1) % spec.bitmaps.size
            currFrame = newFrame
            nextFrameTime += delayBetweenFrames
        }
        currTime = newTime
    }

    override fun render(canvas: Canvas) {
        canvas.drawAnimationTile(this, hitbox, scale)
    }
}
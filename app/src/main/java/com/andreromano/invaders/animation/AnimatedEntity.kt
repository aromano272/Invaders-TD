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
    autoStart: Boolean = false,
) : Entity(
    pos = pos,
    width = spec.tileWidth,
    height = spec.tileHeight,
    posMode = PosMode.CENTER,
) {
    var isRunning = autoStart
        private set
    var shouldStopAtLoopEnd = false

    private val totalFrames = spec.bitmaps.size

    private val delayBetweenFrames = durationMs / totalFrames

    private var currTime = 0L
    private var nextFrameTime = delayBetweenFrames

    var currFrame = 0

    var rotationTetha = 0f

    fun start() {
        shouldStopAtLoopEnd = false
        isRunning = true
    }

    fun stop() {
        shouldStopAtLoopEnd = false
        isRunning = false
        currTime = 0
        nextFrameTime = delayBetweenFrames
        currFrame = 0
    }

    fun stopAtEndOfLoop() {
        if (currFrame == totalFrames - 1) {
            stop()
        } else {
            shouldStopAtLoopEnd = true
        }
    }

    override fun update(deltaTime: Int) {
        if (!isRunning) return
        if (shouldStopAtLoopEnd && currFrame == totalFrames - 1) {
            stop()
            return
        }
        val newTime = currTime + deltaTime
        val timeDiff = newTime - nextFrameTime
        if (timeDiff >= 0) {
            val newFrame = (currFrame + 1) % totalFrames
            currFrame = newFrame
            nextFrameTime += delayBetweenFrames
        }
        currTime = newTime
    }

    override fun render(canvas: Canvas) {
        canvas.drawAnimationTile(this, hitbox, scale)
    }
}
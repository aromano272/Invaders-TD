package com.andreromano.invaders

import android.graphics.Canvas

abstract class Scene(
    val game: Game
) {
    abstract fun updateAndRender(canvas: Canvas, deltaTime: Int)
    abstract fun sceneSizeChanged()
    abstract fun onViewEvent(event: ViewEvent)
}
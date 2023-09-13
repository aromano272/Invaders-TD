package com.andreromano.invaders

import android.graphics.Canvas

interface Scene {
    fun updateAndRender(canvas: Canvas, deltaTime: Int)
    fun sceneSizeChanged(w: Int, h: Int)
    fun onViewEvent(event: ViewEvent)
}
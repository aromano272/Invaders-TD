package com.andreromano.invaders

import android.graphics.Canvas
import android.graphics.RectF
import java.io.Serializable

abstract class Entity(
    var pos: Vec2F,
    var width: Int,
    var height: Int,
    var posMode: PosMode = PosMode.CENTER
) : Serializable {
    private val _rect = RectF()
    val hitbox: RectF
        get() = when (posMode) {
            PosMode.TL -> {
                _rect.apply {
                    left = pos.x
                    top = pos.y
                    right = pos.x + width
                    bottom = pos.y + height
                }
            }
            PosMode.CENTER -> {
                _rect.apply {
                    left = (pos.x - width / 2)
                    top = (pos.y - height / 2)
                    right = (pos.x + width / 2)
                    bottom = (pos.y + height / 2)
                }
            }
        }

    abstract fun update(deltaTime: Int)
    abstract fun render(canvas: Canvas)

    private var clickRegistered = false
    protected fun onClick(callback: () -> Boolean) {
        clickRegistered = true
        ClickListenerRegistry.register(this, callback)
    }

    fun destroy() {
        if (clickRegistered) {
            ClickListenerRegistry.remove(this)
        }
    }

    fun Vec2F.toWorld(): Vec2F = localToWorld(this, pos)

}

enum class PosMode {
    TL,
    CENTER,
}
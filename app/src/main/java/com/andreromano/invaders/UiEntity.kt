package com.andreromano.invaders

import androidx.annotation.CallSuper

abstract class UiEntity(
    private val scene: Scene,
    pos: Vec2F,
    width: Int,
    height: Int,
    posMode: PosMode = PosMode.CENTER,
    val zIndex: Int = 0
) : Entity(
    pos = pos,
    width = width,
    height = height,
    posMode = posMode,
) {
    open fun onClick(x: Float, y: Float): Boolean = false

    private var wasOnClickRegistered: Boolean = false
    @CallSuper
    override fun update(deltaTime: Int) {
        if (!wasOnClickRegistered) {
            ClickListenerRegistry.register(scene, this) { x, y ->
                onClick(x, y)
            }
            wasOnClickRegistered = true
        }
    }
}

package com.andreromano.invaders

import java.lang.ref.WeakReference
import java.util.WeakHashMap

object ClickListenerRegistry {
    private val registry = WeakHashMap<Entity, () -> Boolean>()

    fun onScreenClicked(x: Float, y: Float): Boolean {
        val callback = registry.firstNotNullOfOrNull { (entity, callback) ->
            if (entity.hitbox.contains(x, y)) {
                callback
            } else {
                null
            }
        }
        return if (callback != null) {
            callback()
        } else {
            false
        }
    }

    fun register(entity: Entity, callback: () -> Boolean) {
        registry[entity] = callback
    }

    fun remove(entity: Entity) {
        registry.remove(entity)
    }
}

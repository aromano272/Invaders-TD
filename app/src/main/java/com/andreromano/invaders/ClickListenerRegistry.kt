package com.andreromano.invaders

object ClickListenerRegistry {
    private val registry = mutableListOf<Entry>()

    fun onScreenClicked(x: Float, y: Float): Boolean {
        println("registry: $registry")
        println("registry: ${registry.size}")
        System.gc()
        val entry = registry.firstOrNull { entry ->
            entry.entity.hitbox.contains(x, y)
        }
        return if (entry != null) {
            entry.callback()
        } else {
            false
        }
    }

    fun register(scene: Scene, entity: Entity, callback: () -> Boolean) {
        registry.add(Entry(scene, entity, callback))
    }

    fun remove(entity: Entity) {
        registry.removeIf { entry -> entry.entity == entity }
    }

    fun remove(scene: Scene) {
        registry.removeIf { entry -> entry.scene == scene }
    }
}

class Entry(
    val scene: Scene,
    val entity: Entity,
    val callback: () -> Boolean
)

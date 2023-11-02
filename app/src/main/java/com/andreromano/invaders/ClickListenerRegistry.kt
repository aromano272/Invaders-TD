package com.andreromano.invaders

object ClickListenerRegistry {
    private val registry = mutableListOf<Entry>()

    fun onScreenClicked(x: Float, y: Float): Boolean {
        val entry = registry.firstOrNull { entry ->
            entry.entity.hitbox.contains(x, y)
        }
        return if (entry != null) {
            entry.callback(x, y)
        } else {
            false
        }
    }

    fun register(scene: Scene, entity: Entity, callback: (x: Float, y: Float) -> Boolean) {
        registry.add(Entry(scene, entity, callback))
    }

    fun register(scene: Scene, entity: UiEntity, callback: (x: Float, y: Float) -> Boolean) {
        val indexToInsertTo = registry.indexOfFirst { entry ->
            entry.entity is UiEntity && entry.entity.zIndex <= entity.zIndex
        }.takeIf { it != -1 }

        if (indexToInsertTo != null) {
            registry.add(indexToInsertTo, Entry(scene, entity, callback))
        } else {
            registry.add(Entry(scene, entity, callback))
        }
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
    val callback: (x: Float, y: Float) -> Boolean
)

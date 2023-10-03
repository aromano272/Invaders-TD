package com.andreromano.invaders

import android.graphics.Canvas

class RowEntity(
    pos: Vec2F,
    width: Int,
    height: Int,
    private val children: List<Entity>,
) : Entity(
    pos = pos,
    width = width,
    height = height,
    posMode = PosMode.TL,
) {

    constructor(
        pos: Vec2F,
        width: Int,
        height: Int,
        vararg child: Entity
    ) : this(
        pos, width, height, child.toList()
    )

    private val lastChildrenWidth: MutableList<Int> = mutableListOf()

    private fun layoutChildren() {
//        if (children.filterIndexed { index, entity -> entity.height != lastChildrenWidth.getOrNull(index) }.isEmpty()) return

        var currX = if (width == WRAP_CONTENT) {
            pos.x
        } else {
            val childrenWidth = children.sumOf { it.width }
            pos.x + (width - childrenWidth) / 2
        }
        children.forEach {
            it.pos.x = currX
            it.pos.y = pos.y

            currX += it.width
        }
    }

    override fun update(deltaTime: Int) {
        children.forEach {
            it.update(deltaTime)
        }
    }

    override fun render(canvas: Canvas) {
        layoutChildren()
        children.forEach {
            it.render(canvas)
        }
    }

    companion object {
        const val WRAP_CONTENT = -1
    }
}
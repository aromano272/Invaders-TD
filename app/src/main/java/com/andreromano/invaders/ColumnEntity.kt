package com.andreromano.invaders

import android.graphics.Canvas

class ColumnEntity(
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

    private val lastChildrenHeight: MutableList<Int> = mutableListOf()

    private fun layoutChildren() {
//        if (children.filterIndexed { index, entity -> entity.height != lastChildrenHeight.getOrNull(index) }.isEmpty()) return

        var currY = if (height == WRAP_CONTENT) {
            pos.y
        } else {
            val childrenHeight = children.sumOf { it.height }
            pos.y + (height - childrenHeight) / 2
        }
        children.forEach {
            it.pos.y = currY
            it.pos.x = pos.x

            currY += it.height
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
package com.andreromano.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toRectF
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.extensions.scale

class BulletEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    private val damage: Int,
    private val targetEnemyId: String,
    private val speed: Int,
    private val getEnemyById: (String) -> EnemyEntity?
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height,
) {
    var destroyed: Boolean = false

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    override fun update(deltaTime: Int) {
        val enemy = getEnemyById(targetEnemyId)
        if (enemy == null) {
            destroyed = true
            return
        }

        val moveAmount = (speed * deltaTime) / 5f

        val posDifferenceToEnemy = enemy.pos - pos
        val movDirNorm = posDifferenceToEnemy.normalized()

        val newPos = pos + (movDirNorm * moveAmount)

        // we could instead do `val distanceToEnemy = posDifferenceToEnemy.magnitude` and compare against moveAmount
        // but the .magnitude seems more expensive than these if's
        var collided = false
        if (movDirNorm.x > 0 && newPos.x >= enemy.pos.x) {
            newPos.x = enemy.pos.x
            collided = true
        }
        if (movDirNorm.y > 0 && newPos.y >= enemy.pos.y) {
            newPos.y = enemy.pos.y
            collided = true
        }
        if (movDirNorm.x < 0 && newPos.x <= enemy.pos.x) {
            newPos.x = enemy.pos.x
            collided = true
        }
        if (movDirNorm.y < 0 && newPos.y <= enemy.pos.y) {
            newPos.y = enemy.pos.y
            collided = true
        }

        if (collided) {
            enemy.wasHit(damage)
            this.destroyed = true
        }

        pos = newPos
    }

    override fun render(canvas: Canvas) {
        canvas.drawOval(hitbox.scale(0.25f), paint)
    }
}

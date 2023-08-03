package com.andreromano.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toRectF
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.extensions.scale

class TurretEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    private val getEnemies: () -> List<EnemyEntity>
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height
) {

    private val rangeRadius = hitbox.width() * 1.5f

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GRAY
    }

    private val radiusPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.RED
    }

    var enemyPos: Vec2F = Vec2F.zero()
    var turretToEnemy: Vec2F = Vec2F.zero()

    override fun update(deltaTime: Int) {
        val enemies = getEnemies()
        enemies.forEach { enemy ->
            enemyPos = enemy.pos
            turretToEnemy = enemy.pos - this.pos
            val distanceToTurret = turretToEnemy.magnitude
            enemy.withinTurretRange = distanceToTurret < rangeRadius
        }
    }

    override fun render(canvas: Canvas) {
        canvas.drawOval(hitbox.toRectF().scale(0.60f), paint)
        canvas.drawOval(hitbox.toRectF().scale(3f), radiusPaint)
        canvas.drawLine(
            this.pos.x,
            this.pos.y,
            this.pos.x + turretToEnemy.x,
            this.pos.y + turretToEnemy.y,
            Paint().apply {
                style = Paint.Style.FILL
                color = Color.GREEN
                strokeWidth = 4f
            }
        )
    }
}
package com.andreromano.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toRectF
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawDebugVec
import com.andreromano.invaders.extensions.scale

class TurretEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    private val shootDelay: Int,
    private val getEnemies: () -> List<EnemyEntity>,
    private val spawnBullet: (BulletEntity) -> Unit
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height
) {

    private var bulletSpawnDelay = 0

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
        bulletSpawnDelay -= deltaTime
        val enemies = getEnemies()
        val enemiesWithinRange = enemies.filter { enemy ->
            enemyPos = enemy.pos
            turretToEnemy = enemy.pos - this.pos
            val distanceToTurret = turretToEnemy.magnitude
            val enemyWithinTurretRange = distanceToTurret < rangeRadius
            enemyWithinTurretRange
        }

        // TODO(aromano): debug
        enemies.forEach {
            it.withinTurretRange = enemiesWithinRange.contains(it)
        }

        val targetEnemy = enemiesWithinRange.firstOrNull() ?: return
        if (bulletSpawnDelay > 0) return
        bulletSpawnDelay = shootDelay

        val getEnemyById: (String) -> EnemyEntity? = { id ->
            getEnemies().find { it.id == id }
        }
        val bullet = BulletEntity(pos, 0, 0, width, height, 130, targetEnemy.id, 10, getEnemyById)
        spawnBullet(bullet)
    }

    override fun render(canvas: Canvas) {
        canvas.drawOval(hitbox.scale(0.60f), paint)
        canvas.drawOval(hitbox.scale(3f), radiusPaint)
        canvas.drawDebugVec(this.pos, turretToEnemy, length = 1f)
    }
}
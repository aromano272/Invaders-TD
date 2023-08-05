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
    private val shootDamage: Int,
    private val shootDelay: Int,
    val cost: Int,
    private val rangeRadiusToWidthFactor: Float,
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

    private val rangeRadius = hitbox.width() * rangeRadiusToWidthFactor

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
        if (bulletSpawnDelay > 0) return
        bulletSpawnDelay = shootDelay

        val enemies = getEnemies()
        // TODO(aromano): If there's already a bullet inflight that's gonna kill an enemy, we shouldn't fire more bullets to that enemy
        // find the first enemy within turret range
        val targetEnemy = enemies.firstOrNull { enemy ->
            enemyPos = enemy.pos
            turretToEnemy = enemy.pos - this.pos
            val distanceToTurret = turretToEnemy.magnitude
            val enemyWithinTurretRange = distanceToTurret < rangeRadius
            enemyWithinTurretRange
        } ?: return

        val getEnemyById: (String) -> EnemyEntity? = { id ->
            getEnemies().find { it.id == id }
        }
        val bullet = BulletEntity(pos, 0, 0, width, height, shootDamage, targetEnemy.id, 10, getEnemyById)
        spawnBullet(bullet)
    }

    override fun render(canvas: Canvas) {
        canvas.drawOval(hitbox.scale(0.60f), paint)
        canvas.drawOval(hitbox.scale(rangeRadiusToWidthFactor * 2), radiusPaint)
        canvas.drawDebugVec(this.pos, turretToEnemy, length = 1f)
    }
}
package com.andreromano.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toRectF
import com.andreromano.invaders.Entity
import com.andreromano.invaders.GameState
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawDebugVec
import com.andreromano.invaders.extensions.scale

class TurretEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    val spec: TurretSpec,
    private val spawnBullet: (BulletEntity) -> Unit
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height
) {

    private var bulletSpawnDelay = 0

    private val rangeRadius = hitbox.width() * spec.rangeRadiusToWidthFactor

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = spec.color
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
        bulletSpawnDelay = spec.shootDelay

        val enemies = GameState.enemyEntities
        val enemiesWithinRange = enemies.filter { enemy ->
            enemyPos = enemy.pos
            turretToEnemy = enemy.pos - this.pos
            val distanceToTurret = turretToEnemy.magnitude
            val enemyWithinTurretRange = distanceToTurret < rangeRadius
            enemyWithinTurretRange
        }
        if (enemiesWithinRange.isEmpty()) return

        val getEnemyById: (String) -> EnemyEntity? = { id ->
            GameState.enemyEntities.find { it.id == id }
        }

        if (spec == TurretSpec.SPREADER) {
            val targetEnemies = enemiesWithinRange.shuffled().take(4)

            targetEnemies.forEach { enemy ->
                val bullet = BulletEntity(pos, 0, 0, width, height, spec.shootDamage, enemy.id, 10, getEnemyById)
                spawnBullet(bullet)
            }
        } else {
            // TODO(aromano): If there's already a bullet inflight that's gonna kill an enemy, we shouldn't fire more bullets to that enemy
            // find the first enemy within turret range
            val targetEnemy = enemiesWithinRange.first()

            val bullet = BulletEntity(pos, 0, 0, width, height, spec.shootDamage, targetEnemy.id, 10, getEnemyById)
            spawnBullet(bullet)
        }
    }

    override fun render(canvas: Canvas) {
        canvas.drawOval(hitbox.scale(0.60f), paint)
        canvas.drawOval(hitbox.scale(spec.rangeRadiusToWidthFactor * 2), radiusPaint)
        canvas.drawDebugVec(this.pos, turretToEnemy, length = 1f)
    }
}
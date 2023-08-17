package com.andreromano.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.andreromano.invaders.Entity
import com.andreromano.invaders.GameState
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawDebugRect
import com.andreromano.invaders.drawDebugVec
import com.andreromano.invaders.extensions.scale

class TurretEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    spec: TurretSpec,
    private val spawnBullet: (BulletEntity) -> Unit
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height
) {
    private val upgradeSpec: UpgradeSpec = spec.upgradeSpec
    private val isSpreader = spec == TurretSpec.SPREADER

    var currShootDamage = spec.shootDamage
    var currShootDelay = spec.shootDelay
    var totalMoneySpent = spec.cost
    var upgradeCost: Int = spec.cost
    val sellMoney: Int
        get() = (totalMoneySpent * 0.8f).toInt()
    var currRangeRadiusToWidthFactor = spec.rangeRadiusToWidthFactor

    var currLevel: Int = 1
    val isMaxLevel: Boolean
        get() = currLevel >= upgradeSpec.maxLevel

    private var bulletSpawnDelay = 0

    private val rangeRadius = hitbox.width() * spec.rangeRadiusToWidthFactor

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = spec.color
    }

    private val paintUpgradeLevel = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFD700")
    }

    private val radiusPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.RED
    }

    var enemyPos: Vec2F = Vec2F.zero()
    var turretToEnemy: Vec2F = Vec2F.zero()

    fun upgrade() {
        if (isMaxLevel) return
        if (GameState.currMoney < upgradeCost) return

        GameState.currMoney -= upgradeCost
        currShootDamage = (currShootDamage * upgradeSpec.shootDamageMultiplier).toInt()
        currShootDelay = (currShootDelay * upgradeSpec.shootDelayMultiplier).toInt()
        totalMoneySpent += upgradeCost
        currRangeRadiusToWidthFactor *= upgradeSpec.rangeRadiusToWidthFactorMultiplier
        upgradeCost = (upgradeCost * upgradeSpec.upgradeCostMultiplier).toInt()
        currLevel++
    }

    fun sell() {
        GameState.currMoney += sellMoney
        GameState.entitiesMap[tileY][tileX] = BuildableEntity(pos, tileX, tileY, width, height)
        destroy()
    }

    override fun update(deltaTime: Int) {
        bulletSpawnDelay -= deltaTime
        if (bulletSpawnDelay > 0) return
        bulletSpawnDelay = currShootDelay

        val enemies = GameState.enemyEntities
        val enemiesWithinRange = enemies.filter { enemy ->
            if (enemy.willDieFromIncomingDamage()) return@filter false

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

        if (isSpreader) {
            val targetEnemies = enemiesWithinRange.shuffled().take(4 )

            targetEnemies.forEach { enemy ->
                val bullet = BulletEntity(pos, 0, 0, width, height, currShootDamage, enemy.id, 10, getEnemyById)
                enemy.addIncomingDamage(currShootDamage)
                spawnBullet(bullet)
            }
        } else {
            val enemy = enemiesWithinRange.first()

            val bullet = BulletEntity(pos, 0, 0, width, height, currShootDamage, enemy.id, 10, getEnemyById)
            enemy.addIncomingDamage(currShootDamage)
            spawnBullet(bullet)
        }
    }

    override fun render(canvas: Canvas) {
        canvas.drawOval(hitbox.scale(0.60f), paint)
        canvas.drawOval(hitbox.scale(currRangeRadiusToWidthFactor * 2), radiusPaint)
        repeat(currLevel - 1) { index ->
            val width = width / 10
            val height = width
            val padding = width
            val left = hitbox.left + padding + (width + padding) * index
            val top = hitbox.top + padding
            canvas.drawRect(
                RectF(left, top, left + width, top + height),
                paintUpgradeLevel
            )
        }
        canvas.drawDebugVec(this.pos, turretToEnemy, length = 1f)
    }
}
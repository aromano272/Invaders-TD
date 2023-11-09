package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.TileAtlas
import com.andreromano.invaders.TowerType
import com.andreromano.invaders.scenes.level.levelState
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.angleBetweenYAnd
import com.andreromano.invaders.animation.AnimatedEntity
import com.andreromano.invaders.animation.AnimationSpec
import com.andreromano.invaders.drawTerrainTile
import com.andreromano.invaders.drawTowerEntity
import com.andreromano.invaders.extensions.scale

class TowerWeaponEntity(
    pos: Vec2F,
    animationSpec: AnimationSpec,
    val tower: TowerEntity,
    private val spawnBullet: (BulletEntity) -> Unit
) : AnimatedEntity(
    pos = pos,
    spec = animationSpec,
) {
    private var weaponEntityLevel = -1
    private lateinit var weaponEntity: AnimatedEntity

    private val upgradeSpec: UpgradeSpec = spec.upgradeSpec
    private val isSpreader = spec == TowerSpec.SPREADER

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
    var towerToEnemy: Vec2F = Vec2F.zero()

    fun upgrade() {
        if (isMaxLevel) return
        if (levelState.currMoney < upgradeCost) return

        levelState.currMoney -= upgradeCost
        currShootDamage = (currShootDamage * upgradeSpec.shootDamageMultiplier).toInt()
        currShootDelay = (currShootDelay * upgradeSpec.shootDelayMultiplier).toInt()
        totalMoneySpent += upgradeCost
        currRangeRadiusToWidthFactor *= upgradeSpec.rangeRadiusToWidthFactorMultiplier
        upgradeCost = (upgradeCost * upgradeSpec.upgradeCostMultiplier).toInt()
        currLevel++
    }

    fun restoreUpgradeLevel(level: Int) {
        totalMoneySpent = spec.totalMoneySpentForLevel(level)
        currShootDamage = spec.shootDamageForLevel(level)
        currShootDelay = spec.shootDelayForLevel(level)
        currRangeRadiusToWidthFactor *= spec.rangeRadiusToWidthFactorForLevel(level)
        upgradeCost = spec.upgradeCostForLevel(level)
        currLevel = level
    }

    fun sell() {
        levelState.currMoney += sellMoney
        levelState.entitiesMap[tileY][tileX] = BuildableEntity(pos, tileX, tileY, width, height)
    }

    override fun update(deltaTime: Int) {
        initialiseWeaponEntityIfNeeded()
        weaponEntity.update(deltaTime)
        bulletSpawnDelay -= deltaTime

        val enemies = levelState.enemyEntities
        val enemiesWithinRange = enemies.filter { enemy ->
            if (enemy.willDieFromIncomingDamage()) return@filter false

            enemyPos = enemy.pos
            towerToEnemy = enemy.pos - this.pos
            val distanceToTower = towerToEnemy.magnitude
            val enemyWithinTowerRange = distanceToTower < rangeRadius
            enemyWithinTowerRange
        }
        if (enemiesWithinRange.isEmpty()) return
        val firstEnemy = enemiesWithinRange.first()
        val posDifferenceToEnemy = firstEnemy.pos - pos
        val towerToEnemyDirNorm = posDifferenceToEnemy.normalized()
        weaponEntity.rotationTetha = angleBetweenYAnd(towerToEnemyDirNorm)
        weaponEntity.update(deltaTime)

        if (bulletSpawnDelay > 0) return
        bulletSpawnDelay = currShootDelay

        val getEnemyById: (String) -> EnemyEntity? = { id ->
            levelState.enemyEntities.find { it.id == id }
        }

        if (isSpreader) {
            val targetEnemies = enemiesWithinRange.shuffled().take(4 )

            targetEnemies.forEach { enemy ->
                val bullet = BulletEntity(pos,  width, height, currShootDamage, enemy.id, 10, getEnemyById)
                enemy.addIncomingDamage(currShootDamage)
                spawnBullet(bullet)
            }
        } else {
            val bullet = BulletEntity(pos, width, height, currShootDamage, firstEnemy.id, 10, getEnemyById)
            firstEnemy.addIncomingDamage(currShootDamage)
            spawnBullet(bullet)
        }
    }

    private fun initialiseWeaponEntityIfNeeded() {
        if (weaponEntityLevel == currLevel) return
        weaponEntity = AnimatedEntity(
            pos - Vec2F(0f, height.toFloat() * TileAtlas.towerWeaponBitmapYOffsetPercentOfTowerHeightByLevel[currLevel]!!),
            TileAtlas.towerWeaponAnimSpecs[TowerType.TOWER_1 to currLevel]!!,
            TileAtlas.towerWeaponBitmapScaleFactor
        )
        weaponEntityLevel = currLevel
    }

    override fun render(canvas: Canvas) {
        canvas.drawTerrainTile(this, hitbox)
        canvas.drawTowerEntity(this, hitbox)
        canvas.drawOval(hitbox.scale(currRangeRadiusToWidthFactor * 2), radiusPaint)
        weaponEntity.render(canvas)
    }
}
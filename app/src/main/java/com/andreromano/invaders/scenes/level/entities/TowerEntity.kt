package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.TerrainType
import com.andreromano.invaders.TileAtlas
import com.andreromano.invaders.TiledEntity
import com.andreromano.invaders.scenes.level.levelState
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.drawTerrainTile
import com.andreromano.invaders.drawTowerEntity
import com.andreromano.invaders.extensions.scale

class TowerEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    val spec: TowerSpec,
    private val spawnBullet: (BulletEntity) -> Unit
) : TiledEntity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height,
    terrainType = TerrainType.GRASS
) {
    private lateinit var weaponEntity: TowerWeaponEntity

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

    init {
        initialiseWeaponEntity()
    }

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
        initialiseWeaponEntity()
    }

    fun restoreUpgradeLevel(level: Int) {
        totalMoneySpent = spec.totalMoneySpentForLevel(level)
        currShootDamage = spec.shootDamageForLevel(level)
        currShootDelay = spec.shootDelayForLevel(level)
        currRangeRadiusToWidthFactor *= spec.rangeRadiusToWidthFactorForLevel(level)
        upgradeCost = spec.upgradeCostForLevel(level)
        currLevel = level
        initialiseWeaponEntity()
    }

    fun sell() {
        levelState.currMoney += sellMoney
        levelState.entitiesMap[tileY][tileX] = BuildableEntity(pos, tileX, tileY, width, height)
    }

    private fun initialiseWeaponEntity() {
        weaponEntity = TowerWeaponEntity(
            pos = pos - Vec2F(0f, height.toFloat() * TileAtlas.towerWeaponBitmapYOffsetPercentOfTowerHeightByLevel[currLevel]!!),
            width = width,
            height = height,
            towerPos = pos,
            towerSpec = spec,
            towerLevel = currLevel,
            shootDamage = currShootDamage,
            shootDelay = currShootDelay,
            rangeRadius = rangeRadius,
            spawnBullet = spawnBullet,
        )
    }

    override fun update(deltaTime: Int) {
        weaponEntity.update(deltaTime)
    }

    override fun render(canvas: Canvas) {
        canvas.drawTerrainTile(this, hitbox)
        canvas.drawTowerEntity(this, hitbox)
//        canvas.drawOval(hitbox.scale(currRangeRadiusToWidthFactor * 2), radiusPaint)
        weaponEntity.render(canvas)
    }
}
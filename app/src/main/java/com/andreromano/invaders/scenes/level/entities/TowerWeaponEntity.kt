package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.Entity
import com.andreromano.invaders.TileAtlas
import com.andreromano.invaders.scenes.level.levelState
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.angleBetweenYAnd
import com.andreromano.invaders.animation.AnimatedEntity
import com.andreromano.invaders.animation.AnimationSpec
import com.andreromano.invaders.toAtlasTowerType

class TowerWeaponEntity(
    pos: Vec2F,
    width: Int,
    height: Int,
    private val towerPos: Vec2F,
    private val towerSpec: TowerSpec,
    private val towerLevel: Int,
    private val shootDamage: Int,
    private val shootDelay: Int,
    private val rangeRadius: Float,
    private val spawnBullet: (BulletEntity) -> Unit
) : Entity(
    pos = pos,
    width = width,
    height = height,
) {
    private val idleAnimSpec = TileAtlas.towerWeaponIdleAnimSpecs[towerSpec.toAtlasTowerType() to towerLevel]
    private val shootAnimSpec = TileAtlas.towerWeaponShootAnimSpecs[towerSpec.toAtlasTowerType() to towerLevel]!!
    private val idleAnimEntity = idleAnimSpec?.let {
        AnimatedEntity(
            pos = pos,
            spec = it,
            durationMs = shootDelay,
            scale = TileAtlas.towerWeaponBitmapScaleFactor,
        )
    }
    private val shootAnimEntity = AnimatedEntity(
        pos = pos,
        spec = shootAnimSpec,
        durationMs = shootDelay,
        scale = TileAtlas.towerWeaponBitmapScaleFactor,
    )

    private var isIdle = true

    private val shootsOnFrameNumber = TileAtlas.weaponShootOnFrameNumber[towerSpec.toAtlasTowerType() to towerLevel]
    private val rotatesWithTarget = TileAtlas.weaponRotatesWithTarget(towerSpec.toAtlasTowerType())

    private val radiusPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.RED
    }

    override fun update(deltaTime: Int) {
        idleAnimEntity?.update(deltaTime)
        val enemies = levelState.enemyEntities
        val enemiesWithinRange = enemies.filter { enemy ->
            if (enemy.willDieFromIncomingDamage()) return@filter false

            val towerToEnemy = enemy.pos - towerPos
            val distanceToTower = towerToEnemy.magnitude
            val enemyWithinTowerRange = distanceToTower < rangeRadius
            enemyWithinTowerRange
        }
        isIdle = enemiesWithinRange.isEmpty()
        if (isIdle) {
            shootAnimEntity.stop()
            if (idleAnimEntity != null && !idleAnimEntity.isRunning) idleAnimEntity.start()
            return
        } else {
            idleAnimEntity?.stop()
            if (!shootAnimEntity.isRunning) shootAnimEntity.start()
        }
        val firstEnemy = enemiesWithinRange.first()
        val posDifferenceToEnemy = firstEnemy.pos - towerPos
        val towerToEnemyDirNorm = posDifferenceToEnemy.normalized()
        if (rotatesWithTarget) {
            // TODO: smooth out the rotation
            shootAnimEntity.rotationTetha = angleBetweenYAnd(towerToEnemyDirNorm)
        }

        val oldAnimFrame = shootAnimEntity.currFrame
        shootAnimEntity.update(deltaTime)
        val newAnimFrame = shootAnimEntity.currFrame
        val shouldShoot = shootAnimEntity.isRunning && oldAnimFrame != newAnimFrame && newAnimFrame == shootsOnFrameNumber
        // TODO: maybe not the greatest idea to tie the shooting to the animation but seemed
        //       to be the simplest way of not running the risk of having these go out of sync
        //       and also having the shooting occur on specific frames since not all weapons fire
        //       on the same animation frame
        if (!shouldShoot) return

        val getEnemyById: (String) -> EnemyEntity? = { id ->
            levelState.enemyEntities.find { it.id == id }
        }

        if (towerSpec == TowerSpec.SPREADER) {
            val targetEnemies = enemiesWithinRange.shuffled().take(4 )

            targetEnemies.forEach { enemy ->
                val bullet = BulletEntity(pos,  width, height, shootDamage, enemy.id, 10, getEnemyById)
                enemy.addIncomingDamage(shootDamage)
                spawnBullet(bullet)
            }
        } else {
            val bullet = BulletEntity(pos, width, height, shootDamage, firstEnemy.id, 10, getEnemyById)
            firstEnemy.addIncomingDamage(shootDamage)
            spawnBullet(bullet)
        }
    }

    override fun render(canvas: Canvas) {
        // TODO: i think conceptually every weapon should have an idle and a shoot anim even if there's
        //       no specific idle anim it should just be a single frame, conceptually it just makes sense
        //       for there to be an idle anim rather than rendering the shoot anim but paused,
        //       it would make the logic a bit cleaner than dealing with nullability
        if (idleAnimEntity != null && isIdle) {
            idleAnimEntity.render(canvas)
        } else {
            shootAnimEntity.render(canvas)
        }
    }
}
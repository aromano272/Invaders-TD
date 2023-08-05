package com.andreromano.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Vec2F

class BuildTurretMenuEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    private val getSelectedBuildableEntity: () -> BuildableEntity,
    private val getCurrMoney: () -> Int,
    private val spawnTurret: (TurretEntity) -> Unit,
    private val getEnemies: () -> List<EnemyEntity>,
    private val spawnBullet: (BulletEntity) -> Unit
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height,
) {

    private val turretItemWidth = height

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.DKGRAY
    }

    private val paint1 = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val paint2 = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val itemRects = TurretSpec.values().mapIndexed { index, spec ->
        RectF(
            hitbox.left + index * turretItemWidth,
            hitbox.top,
            hitbox.left + index * turretItemWidth + turretItemWidth,
            hitbox.bottom
        )
    }


    override fun update(deltaTime: Int) {

    }

    override fun render(canvas: Canvas) {
        canvas.drawRect(hitbox, paint)
        itemRects.forEachIndexed { index, rect ->
            canvas.drawRect(rect, if (index % 2 == 0) paint1 else paint2)
        }
    }

    // TODO(aromano): Would be cool to have the turret items be placed in this entity
    //                with this menu coordinates space rather than using world space
    fun onClick(x: Float, y: Float): Boolean {
        itemRects.forEachIndexed { index, rect ->
            if (rect.contains(x, y)) {
                val spec = TurretSpec.values()[index]
                val canBuy = getCurrMoney() >= spec.cost
                if (canBuy) {
                    val buildableEntity = getSelectedBuildableEntity()
                    val turret = TurretEntity(
                        buildableEntity.pos,
                        buildableEntity.tileX,
                        buildableEntity.tileY,
                        buildableEntity.width,
                        buildableEntity.height,
                        spec.shootDamage,
                        spec.shootDelay,
                        spec.cost,
                        spec.rangeRadiusToWidthFactor,
                        getEnemies,
                        spawnBullet
                    )
                    spawnTurret(turret)
                }
                return canBuy
            }
        }
        return false
    }
}

enum class TurretSpec(
    val shootDamage: Int,
    val shootDelay: Int,
    val cost: Int,
    val rangeRadiusToWidthFactor: Float
) {
    FAST(80, 200, 50, 2f),
    STRONG(300, 600, 100, 4f)
}

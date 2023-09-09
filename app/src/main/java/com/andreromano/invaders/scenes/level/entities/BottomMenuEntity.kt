package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.andreromano.invaders.Entity
import com.andreromano.invaders.GameState
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.extensions.toPx

class BottomMenuEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    private val spawnTurret: (TurretEntity) -> Unit,
    private val spawnBullet: (BulletEntity) -> Unit
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height,
) {

    private val buildTurretEntities = TurretSpec.values().mapIndexed { index, spec ->
        val itemWidth = height
        val x = (itemWidth / 2f) + itemWidth * index.toFloat()
        val itemPos = Vec2F(x, pos.y)
        BuildTurretMenuItemEntity(
            itemPos,
            0, 0,
            itemWidth, height,
            spec,
            spawnTurret,
            spawnBullet
        )
    }

    private val turretSelectedEntities = run {
        val itemWidth = height

        val itemCount = 2
        val x = width - (itemWidth / 2f) - itemWidth * (itemCount - 1)
        val startPos = Vec2F(x, pos.y)

        listOf(
            TurretSelectedMenuItemEntity(
                startPos, 0, 0,
                itemWidth, height, true
            ),
            TurretSelectedMenuItemEntity(
                startPos + Vec2F(itemWidth.toFloat(), 0f), 0, 0,
                itemWidth, height, false
            ),
        )
    }
    private val turretSelectedUpgradeEntity = turretSelectedEntities[0]
    private val turretSelectedSellEntity = turretSelectedEntities[1]

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.DKGRAY
    }

    override fun update(deltaTime: Int) {
        val selectedEntity = GameState.selectedEntity ?: return

        when (selectedEntity) {
            is BuildableEntity -> buildTurretEntities.forEach { entity ->
                entity.update(deltaTime)
            }
            is TurretEntity -> {
                if (!selectedEntity.isMaxLevel) turretSelectedUpgradeEntity.update(deltaTime)
                turretSelectedSellEntity.update(deltaTime)
                turretSelectedEntities.forEach { entity ->
                    entity.update(deltaTime)
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        val selectedEntity = GameState.selectedEntity ?: return

        canvas.drawRect(hitbox, paint)
        when (selectedEntity) {
             is BuildableEntity -> buildTurretEntities.forEach { entity ->
                 entity.render(canvas)
             }
             is TurretEntity -> {
                 if (!selectedEntity.isMaxLevel) turretSelectedUpgradeEntity.render(canvas)
                 turretSelectedSellEntity.render(canvas)
             }
        }
    }

    // TODO(aromano): Would be cool to have the turret items be placed in this entity
    //                with this menu coordinates space rather than using world space
}

class BuildTurretMenuItemEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    private val spec: TurretSpec,
    private val spawnTurret: (TurretEntity) -> Unit,
    private val spawnBullet: (BulletEntity) -> Unit,
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height,
) {

    private var enabled = false

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        this.color = spec.color
    }

    private val paintDisabled = Paint().apply {
        style = Paint.Style.FILL
        this.color = Color.parseColor("#CC000000")
    }

    init {
        onClick {
            if (GameState.currMoney < spec.cost) return@onClick true
            val buildableEntity = GameState.selectedEntity as? BuildableEntity ?: return@onClick false
            val turret = TurretEntity(
                buildableEntity.pos,
                buildableEntity.tileX,
                buildableEntity.tileY,
                buildableEntity.width,
                buildableEntity.height,
                spec,
                spawnBullet
            )
            spawnTurret(turret)
            true
        }
    }

    override fun update(deltaTime: Int) {
        enabled = GameState.currMoney >= spec.cost
    }

    override fun render(canvas: Canvas) {
        canvas.drawRect(hitbox, paint)
        if (!enabled) {
            canvas.drawRect(hitbox, paintDisabled)
        }
    }
}

class TurretSelectedMenuItemEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    private val isMenuItemUpgrade: Boolean
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height,
) {

    private var enabled = true

    private var entity: TurretEntity? = null

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        this.color = Color.MAGENTA
    }

    private val paintDisabled = Paint().apply {
        style = Paint.Style.FILL
        this.color = Color.parseColor("#CC000000")
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }


    init {
        onClick {
            if (!enabled) return@onClick false
            val entity = GameState.selectedEntity as? TurretEntity ?: return@onClick false
            if (isMenuItemUpgrade) {
                entity.upgrade()
            } else {
                entity.sell()
            }
            true
        }
    }

    override fun update(deltaTime: Int) {
        entity = (GameState.selectedEntity as? TurretEntity)
        val entity = entity ?: return
        if (isMenuItemUpgrade) {
            enabled = GameState.currMoney >= entity.upgradeCost
        }
    }

    override fun render(canvas: Canvas) {
        val entity = entity ?: return

        canvas.drawRect(hitbox, paint)
        if (!enabled) {
            canvas.drawRect(hitbox, paintDisabled)
        }

        if (isMenuItemUpgrade) {
            canvas.drawText("${entity.upgradeCost}", hitbox.left, pos.y, textPaint)
        } else {
            canvas.drawText("${entity.sellMoney}", hitbox.left, pos.y, textPaint)
        }
    }
}

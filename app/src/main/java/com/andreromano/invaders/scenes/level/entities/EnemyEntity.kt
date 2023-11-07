package com.andreromano.invaders.scenes.level.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.extensions.scale
import java.util.UUID

class EnemyEntity(
    pos: Vec2F,
    width: Int,
    height: Int,
    private val health: Int,
    private val speed: Float,
    val money: Int,
    private val path: List<Waypoint>
) : Entity(
    pos = pos,
    width = width,
    height = height,
) {
    val id = UUID.randomUUID().toString()

    var killed: Boolean = false
    var escaped: Boolean = false
    val destroyed: Boolean
        get() = killed || escaped

    var withinTowerRange = false

    private var currHealth = health
    private var currIncomingDamage = 0

    private var currentWaypoint: Int = 0

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.CYAN
    }

    private val withinTowerRangePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val healthBgPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
        strokeWidth = 6f
    }

    private val healthCurrPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
        strokeWidth = 6f
    }

    override fun update(deltaTime: Int) {
        // Reached end of segment
        if (currentWaypoint == path.size) return

        val moveAmount = speed * deltaTime

        val segment = path[currentWaypoint]
        val posDifferenceToTarget = segment.endPos - pos
        val distanceToTarget = posDifferenceToTarget.magnitude
        val movementDirectionNorm = posDifferenceToTarget.normalized()
        val newPos = pos + (movementDirectionNorm * moveAmount)
        val overshootDistance = moveAmount - distanceToTarget

        // The newPos overshoot the target, so the newPos will be at the target and we'll call update(time) with the remaining time after the initial move
        if (overshootDistance > 0) {
            // Assuming movement speed is linear
            // Swapping the terms of the moveAmount formula to get the time with the overshootDistance
            val remainingMoveTime = overshootDistance / speed
            pos = segment.endPos
            currentWaypoint++

            if (currentWaypoint == path.size) {
                escaped = true
            } else {
                update(remainingMoveTime.toInt())
            }
        } else {
            pos = newPos
        }
    }

    override fun render(canvas: Canvas) {
        canvas.drawOval(hitbox.scale(0.75f), paint)
        if (withinTowerRange) {
            canvas.drawOval(hitbox.scale(0.20f), withinTowerRangePaint)
        }
        if (currHealth < health) {
            val healthPercent = currHealth.toFloat() / health
            val hitboxWidth = hitbox.right - hitbox.left
            val currHealthLineRight = (hitboxWidth * healthPercent) + hitbox.left
            canvas.drawLine(hitbox.left, hitbox.top, hitbox.right, hitbox.top, healthBgPaint)
            canvas.drawLine(hitbox.left, hitbox.top, currHealthLineRight, hitbox.top, healthCurrPaint)
        }
    }

    fun addIncomingDamage(damage: Int) {
        currIncomingDamage += damage
    }

    fun willDieFromIncomingDamage() = currIncomingDamage >= currHealth

    fun wasHit(damage: Int) {
        currIncomingDamage -= damage
        currHealth -= damage / 2
        if (currHealth <= 0) killed = true
    }
}

data class Waypoint(
    val startPos: Vec2F,
    val endPos: Vec2F,
)
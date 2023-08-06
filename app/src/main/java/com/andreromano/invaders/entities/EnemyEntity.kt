package com.andreromano.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toRectF
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Position
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.extensions.scale
import java.util.UUID

class EnemyEntity(
    pos: Vec2F,
    tileX: Int,
    tileY: Int,
    width: Int,
    height: Int,
    private val health: Int,
    private val speed: Float,
    val money: Int,
    private val path: List<PathSegment>
) : Entity(
    pos = pos,
    tileX = tileX,
    tileY = tileY,
    width = width,
    height = height,
) {
    val id = UUID.randomUUID().toString()

    var killed: Boolean = false
    var escaped: Boolean = false
    val destroyed: Boolean
        get() = killed || escaped

    var withinTurretRange = false

    private var currHealth = health

    private var currentPathSegment: Int = 0
    private var currDirection: Vec2F = Vec2F.zero()

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.CYAN
    }

    private val withinTurretRangePaint = Paint().apply {
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
        if (currentPathSegment == path.size) return

        val moveAmount = (speed * deltaTime) / 5f

        val segment = path[currentPathSegment]
        val posDifferenceToTarget = segment.end.pos - pos
        val movementDirectionNorm = posDifferenceToTarget.normalized()
        val newPos = pos + (movementDirectionNorm * moveAmount)

        // TODO(aromano): We're currently handling the case where the newPos would overshoot the segment.end.pos by clamping newPos to the end value
        //                however we should be using the overshoot distance to travel towards the next segment

        var reachedSegmentEnd = false
        if (movementDirectionNorm.x > 0 && newPos.x >= segment.end.pos.x) {
            newPos.x = segment.end.pos.x
            reachedSegmentEnd = true
        }
        if (movementDirectionNorm.y > 0 && newPos.y >= segment.end.pos.y) {
            newPos.y = segment.end.pos.y
            reachedSegmentEnd = true
        }
        if (movementDirectionNorm.x < 0 && newPos.x <= segment.end.pos.x) {
            newPos.x = segment.end.pos.x
            reachedSegmentEnd = true
        }
        if (movementDirectionNorm.y < 0 && newPos.y <= segment.end.pos.y) {
            newPos.y = segment.end.pos.y
            reachedSegmentEnd = true
        }

        if (reachedSegmentEnd) {
            currentPathSegment++
        }

        if (currentPathSegment == path.size) {
            escaped = true
        }

        pos = newPos
    }

    override fun render(canvas: Canvas) {
        canvas.drawOval(hitbox.scale(0.75f), paint)
        if (withinTurretRange) {
            canvas.drawOval(hitbox.scale(0.20f), withinTurretRangePaint)
        }
        if (currHealth < health) {
            val healthPercent = currHealth.toFloat() / health
            val hitboxWidth = hitbox.right - hitbox.left
            val currHealthLineRight = (hitboxWidth * healthPercent) + hitbox.left
            canvas.drawLine(hitbox.left, hitbox.top, hitbox.right, hitbox.top, healthBgPaint)
            canvas.drawLine(hitbox.left, hitbox.top, currHealthLineRight, hitbox.top, healthCurrPaint)
        }
    }

    fun wasHit(damage: Int) {
        currHealth -= damage / 2
        if (currHealth <= 0) killed = true
    }
}

data class PathSegment(
    val start: Position,
    val end: Position
)

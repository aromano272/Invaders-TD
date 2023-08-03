package com.andreromano.invaders

import kotlin.math.sqrt

data class Vec2F(
    var x: Float,
    var y: Float,
) {
    fun toInt(): Vec2 = Vec2(x.toInt(), y.toInt())

    operator fun plus(other: Vec2F): Vec2F = Vec2F(
        x = x + other.x,
        y = y + other.y
    )

    operator fun minus(other: Vec2F): Vec2F = Vec2F(
        x = x - other.x,
        y = y - other. y
    )

    operator fun times(scalar: Float): Vec2F = Vec2F(
        x = x * scalar,
        y = y * scalar
    )

    val magnitude: Float
        get() = sqrt(x * x + y * y)

    // TODO(aromano): should i handle a (0,0) vector and return (0,0) instead of (NaN,NaN) as it's currently doing while trying to divide by 0
    fun normalized(): Vec2F {
        val magnitude = magnitude

        return Vec2F(x / magnitude, y / magnitude)
    }

    companion object {
        fun zero() = Vec2F(0f, 0f)
    }
}

fun dot(a: Vec2F, b: Vec2F): Float {
    // TODO(aromano): Not sure if this is actually true, Freya mentioned that in dot(a, b), one of the vectors needs to be normalized
    //                Actually there are cases where we don't want this but it's move advanced cases so ill keep this here for now
    check(a.magnitude == 1f || b.magnitude == 1f)
    return a.x * b.x + a.y * b.y
}
package com.andreromano.invaders

import kotlin.math.atan2
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

operator fun Float.times(vec: Vec2F): Vec2F = vec * this


fun dot(a: Vec2F, b: Vec2F): Float {
    // TODO(aromano): Not sure if this is actually true, Freya mentioned that in dot(a, b), one of the vectors needs to be normalized
    //                Actually there are cases where we don't want this but it's move advanced cases so ill keep this here for now
//    check(a.magnitude == 1f || b.magnitude == 1f)
    return a.x * b.x + a.y * b.y
}

fun angleBetweenYAnd(a: Vec2F): Float {
    val angle = 90f + Math.toDegrees(atan2(a.y, a.x).toDouble()).toFloat()
    return angle
}

fun reflectByNormal(ray: Vec2F, normal: Vec2F): Vec2F {
    val dotProduct = dot(normal, ray)
    val rayProjectedOntoNormal = normal * dotProduct
    val result = ray - rayProjectedOntoNormal * 2f
    return result
}

// Currently we're only taking in consideration translation of the local space, disregarding rotation and scaling
fun localToWorld(local: Vec2F, localOrigin: Vec2F): Vec2F {
    return localOrigin + local
}
fun worldToLocal(world: Vec2F, localOrigin: Vec2F): Vec2F {
    return world - localOrigin
}


//data class Transform(
//    val pos: Vec2F
//) {
//    val up: Vec2F = Vec2F(0f, 1f)
//    val right: Vec2F = Vec2F(1f, 0f)
//}
//fun localToWorld(local: Vec2F, transform: Transform): Vec2F {
//    var world = transform.pos
//    world += local.x * transform.right // x axis
//    world += local.y * transform.up // y axis
//    return world
//}
//fun worldToLocal(world: Vec2F, transform: Transform): Vec2F {
//    val rel = world - transform.pos
//    val x = dot(rel, transform.right) // x axis
//    val y = dot(rel, transform.up) // y axis
//    return Vec2F(x, y)
//}

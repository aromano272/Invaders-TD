package com.andreromano.invaders

abstract class TiledEntity(
    pos: Vec2F,
    var tileX: Int,
    var tileY: Int,
    width: Int,
    height: Int,
) : Entity(
    pos,
    width,
    height,
)
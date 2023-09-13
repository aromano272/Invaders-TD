package com.andreromano.invaders

import java.io.Serializable

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
), Serializable
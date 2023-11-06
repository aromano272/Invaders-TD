package com.andreromano.invaders

abstract class TiledEntity(
    pos: Vec2F,
    var tileX: Int,
    var tileY: Int,
    width: Int,
    height: Int,
    val terrainType: TerrainType,
) : Entity(
    pos,
    width,
    height,
) {
    lateinit var tileEdges: TileEdges
}
package com.andreromano.invaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.toRectF
import com.andreromano.invaders.animation.AnimatedEntity
import com.andreromano.invaders.animation.AnimationSpec
import com.andreromano.invaders.extensions.copy
import com.andreromano.invaders.scenes.level.entities.TowerEntity
import com.andreromano.invaders.scenes.level.entities.TowerSpec
import kotlin.math.abs

object TileAtlas {
    lateinit var tower1Bitmap: Bitmap
        private set

    lateinit var terrainBitmap: Bitmap
        private set

    lateinit var tower1WeaponAnimBitmap: Bitmap
        private set

    private val towerNumColTiles = 3
    var towerTileWidth: Int = -1
    var towerTileHeight: Int = -1

    val towerWeaponAnimNumRowTiles = 6
    var towerWeaponAnimTileSize: Int = -1

    private val terrainNumRowTiles = 16
    var terrainTileSize: Int = -1

    fun initialise(context: Context) {
        terrainBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.grass_tileset,
            BitmapFactory.Options().apply {
                inScaled = false
            }
        )
        terrainTileSize = terrainBitmap.height / terrainNumRowTiles

        tower1Bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.tower_01,
            BitmapFactory.Options().apply {
                inScaled = false
            }
        )
        towerTileWidth = tower1Bitmap.width / towerNumColTiles
        towerTileHeight = tower1Bitmap.height

        tower1WeaponAnimBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.tower_01___level_01___weapon,
            BitmapFactory.Options().apply {
                inScaled = false
            }
        )
        towerWeaponAnimTileSize = tower1WeaponAnimBitmap.width / towerWeaponAnimNumRowTiles
    }
}

enum class TerrainType {
    DIRT,
    GRASS,
}

data class TileEdges(
    val top: TerrainType,
    val right: TerrainType,
    val bottom: TerrainType,
    val left: TerrainType,
)

data class TilePos(
    val x: Int,
    val y: Int,
)

val terrainTileEdgesToTilePos: Map<TileEdges, TilePos> = mapOf(
    TileEdges(TerrainType.GRASS, TerrainType.DIRT, TerrainType.DIRT, TerrainType.GRASS) to TilePos(5, 1),
    TileEdges(TerrainType.GRASS, TerrainType.GRASS, TerrainType.DIRT, TerrainType.DIRT) to TilePos(7, 1),
    TileEdges(TerrainType.DIRT, TerrainType.DIRT, TerrainType.GRASS, TerrainType.GRASS) to TilePos(5, 3),
    TileEdges(TerrainType.DIRT, TerrainType.GRASS, TerrainType.GRASS, TerrainType.DIRT) to TilePos(7, 3),

    TileEdges(TerrainType.GRASS, TerrainType.GRASS, TerrainType.DIRT, TerrainType.GRASS) to TilePos(2, 6),
    TileEdges(TerrainType.GRASS, TerrainType.DIRT, TerrainType.GRASS, TerrainType.GRASS) to TilePos(1, 7),
    TileEdges(TerrainType.GRASS, TerrainType.GRASS, TerrainType.GRASS, TerrainType.DIRT) to TilePos(3, 7),
    TileEdges(TerrainType.DIRT, TerrainType.GRASS, TerrainType.GRASS, TerrainType.GRASS) to TilePos(2, 8),

    TileEdges(TerrainType.DIRT, TerrainType.GRASS, TerrainType.DIRT, TerrainType.GRASS) to TilePos(5, 2),
    TileEdges(TerrainType.GRASS, TerrainType.DIRT, TerrainType.GRASS, TerrainType.DIRT) to TilePos(6, 1),

    TileEdges(TerrainType.GRASS, TerrainType.GRASS, TerrainType.GRASS, TerrainType.GRASS) to TilePos(6, 2),
    TileEdges(TerrainType.DIRT, TerrainType.DIRT, TerrainType.DIRT, TerrainType.DIRT) to TilePos(9, 2),
)

val TerrainType.terrainTypeTilePos: TilePos
    get() = when (this) {
        TerrainType.GRASS -> TilePos(6, 2)
        TerrainType.DIRT -> TilePos(9, 2)
    }

val obstaclesTilesPos = listOf(
    TilePos(13, 6),
    TilePos(14, 6),
    TilePos(13, 7),
    TilePos(14, 7),

    TilePos(13, 9),
    TilePos(14, 9),
    TilePos(13, 10),
    TilePos(14, 10),

    TilePos(13, 12),
    TilePos(14, 12),
    TilePos(13, 13),
    TilePos(14, 13),
)

fun Canvas.drawTerrainTile(entity: TiledEntity, destRect: RectF) {
    val tilePos = when (entity.terrainType) {
        TerrainType.GRASS -> entity.terrainType.terrainTypeTilePos
        TerrainType.DIRT -> terrainTileEdgesToTilePos[entity.tileEdges]
            ?: throw IllegalStateException("${entity.tileEdges} not found in terrainTileEdgesToTilePos")
    }

    drawTerrainTile(tilePos, destRect)
}

fun Canvas.drawTowerEntity(entity: TowerEntity, destRect: RectF) {
    val towerBitmap = when (entity.spec) {
        TowerSpec.FAST -> TileAtlas.tower1Bitmap
        TowerSpec.STRONG -> TODO()
        TowerSpec.SPREADER -> TODO()
    }
    val tilePos = TilePos(entity.currLevel - 1, 0)

    drawTowerTile(towerBitmap, tilePos, destRect)
}

fun Canvas.drawObstacleTile(randomSeed: Int, destRect: RectF) {
    drawTerrainTile(obstaclesTilesPos[abs(randomSeed) % obstaclesTilesPos.size], destRect)
}

private fun Canvas.drawTerrainTile(tilePos: TilePos, destRect: RectF) {
    val tileSize = TileAtlas.terrainTileSize
    val x = tilePos.x * tileSize
    val y = tilePos.y * tileSize
    this.drawBitmap(
        TileAtlas.terrainBitmap,
        Rect(
            x,
            y,
            x + tileSize,
            y + tileSize
        ),
        destRect,
        null
    )
}

val tower1WeaponAnim: AnimationSpec by lazy {
    AnimationSpec(
        bitmap = TileAtlas.tower1WeaponAnimBitmap,
        numFrames = TileAtlas.towerWeaponAnimNumRowTiles,
        tileSize = TileAtlas.towerWeaponAnimTileSize,
        durationMs = 1000,
    )
}

fun Canvas.drawAnimationTile(entity: AnimatedEntity, destRect: RectF) {
    val sourceRect = Rect(
        0,
        0,
        entity.spec.tileSize,
        entity.spec.tileSize,
    )

    val matrix = Matrix().apply {
        setRectToRect(sourceRect.toRectF(), destRect, Matrix.ScaleToFit.CENTER)
        postRotate(entity.rotationDeg, destRect.centerX(), destRect.centerY())
    }

    this.drawBitmap(
        entity.spec.bitmaps[entity.currTileCol],
        matrix,
        null
    )
}

fun Canvas.drawDebugTile(destRect: RectF) {
    val tileSize = TileAtlas.terrainTileSize
    val x = obstaclesTilesPos[0].x * tileSize
    val y = obstaclesTilesPos[0].y * tileSize
    val sourceRect = Rect(
        x,
        y,
        x + tileSize,
        y + tileSize
    )

    val matrix = Matrix().apply {
        setRectToRect(sourceRect.toRectF(), destRect, Matrix.ScaleToFit.CENTER)
//        postRotate(90f, destRect.centerX(), destRect.centerY())
    }

    this.drawBitmap(
        TileAtlas.terrainBitmap,
        matrix,
        null
    )
}

private fun Canvas.drawTowerTile(bitmap: Bitmap, tilePos: TilePos, destRect: RectF) {
    val towerTileWidth = TileAtlas.towerTileWidth
    val towerTileHeight = TileAtlas.towerTileHeight
    val x = tilePos.x * towerTileWidth
    val y = tilePos.y * towerTileHeight
    this.drawBitmap(
        bitmap,
        Rect(
            x,
            y,
            x + towerTileWidth,
            y + towerTileHeight
        ),
        destRect.copy(top = destRect.top - destRect.height()),
        null
    )
}


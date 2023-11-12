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
import com.andreromano.invaders.extensions.scale
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
        terrainBitmap = context.decodeBitmap(R.drawable.grass_tileset)
        terrainTileSize = terrainBitmap.height / terrainNumRowTiles

        tower1Bitmap = context.decodeBitmap(R.drawable.tower_01)
        towerTileWidth = tower1Bitmap.width / towerNumColTiles
        towerTileHeight = tower1Bitmap.height

        tower1WeaponAnimBitmap = context.decodeBitmap(R.drawable.tower_01___level_01___weapon)
        towerWeaponAnimTileSize = tower1WeaponAnimBitmap.width / towerWeaponAnimNumRowTiles

        initialiseTowers(context)
    }

    val towerWeaponBitmapScaleFactor = 1.5f
    val towerWeaponBitmapYOffsetPercentOfTowerHeightByLevel: Map<Int, Float> = mapOf(
        1 to 0.70f,
        2 to 0.75f,
        3 to 0.80f,
    )

    val towerBitmaps = mutableMapOf<TowerType, Bitmap>()
    val towerWeaponBitmaps = mutableMapOf<Pair<TowerType, Int>, Bitmap>()
    val towerWeaponAnimSpecs = mutableMapOf<Pair<TowerType, Int>, AnimationSpec>()
    val towerProjectileBitmaps = mutableMapOf<Pair<TowerType, Int>, Bitmap>()
    val towerProjectileImpactBitmaps = mutableMapOf<Pair<TowerType, Int>, Bitmap>()
    private fun initialiseTowers(context: Context) {
        towerBitmaps[TowerType.TOWER_1] = context.decodeBitmap(R.drawable.tower_01)
        towerProjectileBitmaps[TowerType.TOWER_1 to 1] = context.decodeBitmap(R.drawable.tower_01___level_01___projectile)
        towerWeaponBitmaps[TowerType.TOWER_1 to 1] = context.decodeBitmap(R.drawable.tower_01___level_01___weapon)
        towerProjectileBitmaps[TowerType.TOWER_1 to 2] = context.decodeBitmap(R.drawable.tower_01___level_02___projectile)
        towerWeaponBitmaps[TowerType.TOWER_1 to 2] = context.decodeBitmap(R.drawable.tower_01___level_02___weapon)
        towerProjectileBitmaps[TowerType.TOWER_1 to 3] = context.decodeBitmap(R.drawable.tower_01___level_03___projectile)
        towerWeaponBitmaps[TowerType.TOWER_1 to 3] = context.decodeBitmap(R.drawable.tower_01___level_03___weapon)
        towerProjectileImpactBitmaps[TowerType.TOWER_1 to 1] = context.decodeBitmap(R.drawable.tower_01___weapon___impact)
        towerProjectileImpactBitmaps[TowerType.TOWER_1 to 2] = context.decodeBitmap(R.drawable.tower_01___weapon___impact)
        towerProjectileImpactBitmaps[TowerType.TOWER_1 to 3] = context.decodeBitmap(R.drawable.tower_01___weapon___impact)
        towerBitmaps[TowerType.TOWER_2] = context.decodeBitmap(R.drawable.tower_02)
        towerProjectileBitmaps[TowerType.TOWER_2 to 1] = context.decodeBitmap(R.drawable.tower_02___level_01___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_2 to 1] = context.decodeBitmap(R.drawable.tower_02___level_01___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_2 to 1] = context.decodeBitmap(R.drawable.tower_02___level_01___weapon)
        towerProjectileBitmaps[TowerType.TOWER_2 to 2] = context.decodeBitmap(R.drawable.tower_02___level_02___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_2 to 2] = context.decodeBitmap(R.drawable.tower_02___level_02___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_2 to 2] = context.decodeBitmap(R.drawable.tower_02___level_02___weapon)
        towerProjectileBitmaps[TowerType.TOWER_2 to 3] = context.decodeBitmap(R.drawable.tower_02___level_03___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_2 to 3] = context.decodeBitmap(R.drawable.tower_02___level_03___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_2 to 3] = context.decodeBitmap(R.drawable.tower_02___level_03___weapon)
        towerBitmaps[TowerType.TOWER_3] = context.decodeBitmap(R.drawable.tower_03)
        towerProjectileBitmaps[TowerType.TOWER_3 to 1] = context.decodeBitmap(R.drawable.tower_03___level_01___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_3 to 1] = context.decodeBitmap(R.drawable.tower_03___level_01___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_3 to 1] = context.decodeBitmap(R.drawable.tower_03___level_01___weapon)
        towerProjectileBitmaps[TowerType.TOWER_3 to 2] = context.decodeBitmap(R.drawable.tower_03___level_02___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_3 to 2] = context.decodeBitmap(R.drawable.tower_03___level_02___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_3 to 2] = context.decodeBitmap(R.drawable.tower_03___level_02___weapon)
        towerProjectileBitmaps[TowerType.TOWER_3 to 3] = context.decodeBitmap(R.drawable.tower_03___level_03___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_3 to 3] = context.decodeBitmap(R.drawable.tower_03___level_03___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_3 to 3] = context.decodeBitmap(R.drawable.tower_03___level_03___weapon)
        towerBitmaps[TowerType.TOWER_4] = context.decodeBitmap(R.drawable.tower_04)
        towerProjectileBitmaps[TowerType.TOWER_4 to 1] = context.decodeBitmap(R.drawable.tower_04___level_01___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_4 to 1] = context.decodeBitmap(R.drawable.tower_04___level_01___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_4 to 1] = context.decodeBitmap(R.drawable.tower_04___level_01___weapon)
        towerProjectileBitmaps[TowerType.TOWER_4 to 2] = context.decodeBitmap(R.drawable.tower_04___level_02___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_4 to 2] = context.decodeBitmap(R.drawable.tower_04___level_02___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_4 to 2] = context.decodeBitmap(R.drawable.tower_04___level_02___weapon)
        towerProjectileBitmaps[TowerType.TOWER_4 to 3] = context.decodeBitmap(R.drawable.tower_04___level_03___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_4 to 3] = context.decodeBitmap(R.drawable.tower_04___level_03___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_4 to 3] = context.decodeBitmap(R.drawable.tower_04___level_03___weapon)
        towerBitmaps[TowerType.TOWER_5] = context.decodeBitmap(R.drawable.tower_05)
        towerProjectileBitmaps[TowerType.TOWER_5 to 1] = context.decodeBitmap(R.drawable.tower_05___level_01___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_5 to 1] = context.decodeBitmap(R.drawable.tower_05___level_01___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_5 to 1] = context.decodeBitmap(R.drawable.tower_05___level_01___weapon)
        towerProjectileBitmaps[TowerType.TOWER_5 to 2] = context.decodeBitmap(R.drawable.tower_05___level_02___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_5 to 2] = context.decodeBitmap(R.drawable.tower_05___level_02___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_5 to 2] = context.decodeBitmap(R.drawable.tower_05___level_02___weapon)
        towerProjectileBitmaps[TowerType.TOWER_5 to 3] = context.decodeBitmap(R.drawable.tower_05___level_03___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_5 to 3] = context.decodeBitmap(R.drawable.tower_05___level_03___projectile___impact)
        towerWeaponBitmaps[TowerType.TOWER_5 to 3] = context.decodeBitmap(R.drawable.tower_05___level_03___weapon)
        towerBitmaps[TowerType.TOWER_6] = context.decodeBitmap(R.drawable.tower_06)
        towerProjectileBitmaps[TowerType.TOWER_6 to 1] = context.decodeBitmap(R.drawable.tower_06___level_01___projectile)
        towerWeaponBitmaps[TowerType.TOWER_6 to 1] = context.decodeBitmap(R.drawable.tower_06___level_01___weapon)
        towerProjectileBitmaps[TowerType.TOWER_6 to 2] = context.decodeBitmap(R.drawable.tower_06___level_02___projectile)
        towerWeaponBitmaps[TowerType.TOWER_6 to 2] = context.decodeBitmap(R.drawable.tower_06___level_02___weapon)
        towerProjectileBitmaps[TowerType.TOWER_6 to 3] = context.decodeBitmap(R.drawable.tower_06___level_03___projectile)
        towerWeaponBitmaps[TowerType.TOWER_6 to 3] = context.decodeBitmap(R.drawable.tower_06___level_03___weapon)
        towerProjectileImpactBitmaps[TowerType.TOWER_6 to 1] = context.decodeBitmap(R.drawable.tower_06___weapon___impact)
        towerProjectileImpactBitmaps[TowerType.TOWER_6 to 2] = context.decodeBitmap(R.drawable.tower_06___weapon___impact)
        towerProjectileImpactBitmaps[TowerType.TOWER_6 to 3] = context.decodeBitmap(R.drawable.tower_06___weapon___impact)
        towerBitmaps[TowerType.TOWER_7] = context.decodeBitmap(R.drawable.tower_07)
        towerWeaponBitmaps[TowerType.TOWER_7 to 1] = context.decodeBitmap(R.drawable.tower_07___level_01___weapon)
        towerWeaponBitmaps[TowerType.TOWER_7 to 2] = context.decodeBitmap(R.drawable.tower_07___level_02___weapon)
        towerWeaponBitmaps[TowerType.TOWER_7 to 3] = context.decodeBitmap(R.drawable.tower_07___level_03___weapon)
        towerProjectileBitmaps[TowerType.TOWER_7 to 1] = context.decodeBitmap(R.drawable.tower_07___level_x___projectile)
        towerProjectileBitmaps[TowerType.TOWER_7 to 2] = context.decodeBitmap(R.drawable.tower_07___level_x___projectile)
        towerProjectileBitmaps[TowerType.TOWER_7 to 3] = context.decodeBitmap(R.drawable.tower_07___level_x___projectile)
        towerProjectileImpactBitmaps[TowerType.TOWER_7 to 1] = context.decodeBitmap(R.drawable.tower_07___level_x___projectile___impact)
        towerProjectileImpactBitmaps[TowerType.TOWER_7 to 2] = context.decodeBitmap(R.drawable.tower_07___level_x___projectile___impact)
        towerProjectileImpactBitmaps[TowerType.TOWER_7 to 3] = context.decodeBitmap(R.drawable.tower_07___level_x___projectile___impact)
        towerBitmaps[TowerType.TOWER_8] = context.decodeBitmap(R.drawable.tower_08)
        towerProjectileBitmaps[TowerType.TOWER_8 to 1] = context.decodeBitmap(R.drawable.tower_08___level_01___projectile)
        towerWeaponBitmaps[TowerType.TOWER_8 to 1] = context.decodeBitmap(R.drawable.tower_08___level_01___weapon)
        towerProjectileBitmaps[TowerType.TOWER_8 to 2] = context.decodeBitmap(R.drawable.tower_08___level_02___projectile)
        towerWeaponBitmaps[TowerType.TOWER_8 to 2] = context.decodeBitmap(R.drawable.tower_08___level_02___weapon)
        towerProjectileBitmaps[TowerType.TOWER_8 to 3] = context.decodeBitmap(R.drawable.tower_08___level_03___projectile)
        towerWeaponBitmaps[TowerType.TOWER_8 to 3] = context.decodeBitmap(R.drawable.tower_08___level_03___weapon)

        towerWeaponBitmaps.forEach { (key, bitmap) ->
            towerWeaponAnimSpecs[key] = genericAnimSpec(bitmap, key)
        }
    }

    private fun Context.decodeBitmap(resId: Int) = BitmapFactory.decodeResource(
        this.resources,
        resId,
        BitmapFactory.Options().apply {
            inScaled = false
        }
    )
}

enum class TowerType {
    TOWER_1,
    TOWER_2,
    TOWER_3,
    TOWER_4,
    TOWER_5,
    TOWER_6,
    TOWER_7,
    TOWER_8,
}

fun TowerSpec.toAtlasTowerType(): TowerType = when (this) {
    TowerSpec.FAST -> TowerType.TOWER_1
    TowerSpec.STRONG -> TowerType.TOWER_2
    TowerSpec.SPREADER -> TowerType.TOWER_7
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

private fun genericAnimSpec(bitmap: Bitmap, debugInfo: Pair<TowerType, Int>): AnimationSpec {
    check(bitmap.width > bitmap.height)
    check(bitmap.width % bitmap.height == 0) {
        "$debugInfo"
    }
    val numFrames = bitmap.width / bitmap.height
    val tileSize = bitmap.height
    return AnimationSpec(
        bitmap = bitmap,
        numFrames = numFrames,
        tileSize = tileSize,
    )
}

fun Canvas.drawAnimationTile(entity: AnimatedEntity, destRect: RectF, scale: Float) {
    val sourceRect = Rect(
        0,
        0,
        entity.spec.tileSize,
        entity.spec.tileSize,
    )

    val matrix = Matrix().apply {
        setRectToRect(sourceRect.toRectF(), destRect.scale(scale), Matrix.ScaleToFit.CENTER)
        postRotate(entity.rotationTetha, destRect.centerX(), destRect.centerY())
    }

    this.drawBitmap(
        entity.spec.bitmaps[entity.currFrame],
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


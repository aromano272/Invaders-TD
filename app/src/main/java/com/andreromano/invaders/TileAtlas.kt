package com.andreromano.invaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.get
import androidx.core.graphics.toRectF
import com.andreromano.invaders.animation.AnimatedEntity
import com.andreromano.invaders.animation.AnimationSpec
import com.andreromano.invaders.extensions.copy
import com.andreromano.invaders.extensions.scale
import com.andreromano.invaders.scenes.level.entities.TowerEntity
import com.andreromano.invaders.scenes.level.entities.TowerSpec
import kotlin.math.abs
import kotlin.system.measureTimeMillis

object TileAtlas {
    lateinit var terrainBitmap: Bitmap
        private set

    private val terrainNumRowTiles = 16
    var terrainTileSize: Int = -1

    fun initialise(context: Context) {
        terrainBitmap = context.decodeBitmap(R.drawable.grass_tileset)
        terrainTileSize = terrainBitmap.height / terrainNumRowTiles

        initialiseTowers(context)
    }

    val towerWeaponBitmapScaleFactor = 1.5f
    val towerWeaponBitmapYOffsetPercentOfTowerHeightByLevel: Map<Int, Float> = mapOf(
        1 to (0.5f + 0.125f),
        2 to (0.5f + 0.266f),
        3 to (0.5f + 0.406f),
    )

    val weaponShootOnFrameNumber: Map<Pair<TowerType, Int>, Int> = mapOf(
        (TowerType.TOWER_1 to 1) to 2,
        (TowerType.TOWER_1 to 2) to 2,
        (TowerType.TOWER_1 to 3) to 2,
        (TowerType.TOWER_2 to 1) to 6,
        (TowerType.TOWER_2 to 2) to 9,
        (TowerType.TOWER_2 to 3) to 11,
    )

    fun weaponRotatesWithTarget(type: TowerType): Boolean = when (type) {
        TowerType.TOWER_1 -> true
        TowerType.TOWER_2 -> false
        TowerType.TOWER_3 -> true
        TowerType.TOWER_4 -> true
        TowerType.TOWER_5 -> true
        TowerType.TOWER_6 -> true
        TowerType.TOWER_7 -> true
        TowerType.TOWER_8 -> true
    }

    val towerBitmaps = mutableMapOf<TowerType, Bitmap>()
    val towerWeaponBitmaps = mutableMapOf<Pair<TowerType, Int>, Bitmap>()
    val towerWeaponIdleAnimSpecs = mutableMapOf<Pair<TowerType, Int>, AnimationSpec>()
    val towerWeaponShootAnimSpecs = mutableMapOf<Pair<TowerType, Int>, AnimationSpec>()
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
            val hasIdleAnim = key.first in listOf(TowerType.TOWER_2, TowerType.TOWER_5)

            if (hasIdleAnim) {
                val (idleAnim, shootAnim) = genericTwoRowAnimSpec(bitmap, key)
                towerWeaponIdleAnimSpecs[key] = idleAnim
                towerWeaponShootAnimSpecs[key] = shootAnim
            } else {
                towerWeaponShootAnimSpecs[key] = genericAnimSpec(bitmap, key)
            }
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
    val atlasTowerType = entity.spec.toAtlasTowerType()
    val towerBitmap = TileAtlas.towerBitmaps[atlasTowerType]!!
    val tilePos = TilePos(entity.currLevel - 1, 0)

    drawTowerTile(towerBitmap, entity.spec.upgradeSpec.maxLevel, tilePos, destRect)
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

private fun genericTwoRowAnimSpec(bitmap: Bitmap, debugInfo: Pair<TowerType, Int>): Pair<AnimationSpec, AnimationSpec> {
    val topBitmap = Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height / 2,
    )
    val bottomBitmap = Bitmap.createBitmap(
        bitmap,
        0,
        bitmap.height / 2,
        bitmap.width,
        bitmap.height / 2,
    )
    val idleAnimSpec = genericAnimSpec(topBitmap, debugInfo)
    val shootAnimSpec = genericAnimSpec(bottomBitmap, debugInfo)

    val transparentBitmap = Bitmap.createBitmap(
        idleAnimSpec.tileSize,
        idleAnimSpec.tileSize,
        idleAnimSpec.bitmaps.first().config
    )

    // remove empty frames since the idle and shoot anims might not have the same frame count
    val idleSanitizedAnimBitmaps = idleAnimSpec.bitmaps.filter {
        !it.sameAs(transparentBitmap)
    }
    val shootSanitizedAnimBitmaps = shootAnimSpec.bitmaps.filter {
        !it.sameAs(transparentBitmap)
    }

    val idleSanitizedAnimSpec = AnimationSpec(idleSanitizedAnimBitmaps, idleAnimSpec.tileSize)
    val shootSanitizedAnimSpec = AnimationSpec(shootSanitizedAnimBitmaps, shootAnimSpec.tileSize)
    return idleSanitizedAnimSpec to shootSanitizedAnimSpec
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

private fun Canvas.drawTowerTile(bitmap: Bitmap, numTotalTiles: Int, tilePos: TilePos, destRect: RectF) {
    check(bitmap.width % numTotalTiles.toFloat() == 0f)

    val towerTileWidth = bitmap.width / numTotalTiles
    val towerTileHeight = bitmap.height
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


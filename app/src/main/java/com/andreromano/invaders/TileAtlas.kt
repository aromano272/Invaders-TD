package com.andreromano.invaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF

object TileAtlas {
    lateinit var bitmap: Bitmap
        private set

    private val colCount = 23
    private val rowCount = 13
    var tileSize: Int = -1

    fun initialise(context: Context) {
        bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.tileatlas,
            BitmapFactory.Options().apply {
                inScaled = false
            }
        )
        tileSize = bitmap.height / rowCount
    }
}

fun Canvas.drawBuildableTile(nineSliceIndex: Int, destRect: RectF) {
    val centerOfTileX = 1
    val centerOfTileY = 4
    val (tileX, tileY) = when (nineSliceIndex) {
        0 -> (centerOfTileX - 1) to (centerOfTileY - 1)
        1 -> (centerOfTileX - 0) to (centerOfTileY - 1)
        2 -> (centerOfTileX + 1) to (centerOfTileY - 1)
        3 -> (centerOfTileX - 1) to (centerOfTileY - 0)
        4 -> (centerOfTileX - 0) to (centerOfTileY - 0)
        5 -> (centerOfTileX + 1) to (centerOfTileY - 0)
        6 -> (centerOfTileX - 1) to (centerOfTileY + 1)
        7 -> (centerOfTileX - 0) to (centerOfTileY + 1)
        8 -> (centerOfTileX + 1) to (centerOfTileY + 1)
        else -> throw IllegalStateException()
    }

    drawTile(tileX, tileY, destRect)
}

fun Canvas.drawTile(tileX: Int, tileY: Int, destRect: RectF) {
    val tileSize = TileAtlas.tileSize
    val x = tileX * tileSize
    val y = tileY * tileSize
    this.drawBitmap(
        TileAtlas.bitmap,
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
package com.andreromano.invaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF

object TileMap {
    lateinit var bitmap: Bitmap
        private set

    private val colCount = 23
    private val rowCount = 13
    var tileSize: Int = -1

    fun initialise(context: Context) {
        bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.tilemap,
            BitmapFactory.Options().apply {
                inScaled = false
            }
        )
        tileSize = bitmap.height / rowCount
    }
}

fun Canvas.drawTile(tileX: Int, tileY: Int, destRect: RectF) {
    val tileSize = TileMap.tileSize
    val x = tileX * tileSize
    val y = tileY * tileSize
    this.drawBitmap(
        TileMap.bitmap,
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
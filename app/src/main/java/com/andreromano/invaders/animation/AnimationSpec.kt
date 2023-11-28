package com.andreromano.invaders.animation

import android.graphics.Bitmap

data class AnimationSpec(
    val bitmaps: List<Bitmap>,
    val tileWidth: Int,
    val tileHeight: Int,
) {
    constructor(
        bitmap: Bitmap,
        numFrames: Int,
        tileWidth: Int,
        tileHeight: Int,
    ) : this(
        getFrames(bitmap, numFrames, tileWidth, tileHeight),
        tileWidth,
        tileHeight,
    )
    companion object {
        fun getFrames(bitmap: Bitmap, numColTiles: Int, tileWidth: Int, tileHeight: Int): List<Bitmap> =
            (0 until numColTiles).map { index ->
                val x = index * tileWidth
                val y = 0
                Bitmap.createBitmap(
                    bitmap,
                    x,
                    y,
                    tileWidth,
                    tileHeight,
                )
            }
    }
}
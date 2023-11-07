package com.andreromano.invaders.animation

import android.graphics.Bitmap

data class AnimationSpec(
    val bitmaps: List<Bitmap>,
    val tileSize: Int,
    val durationMs: Int,
) {
    constructor(
        bitmap: Bitmap,
        numFrames: Int,
        tileSize: Int,
        durationMs: Int,
    ) : this(
        getFrames(bitmap, numFrames, tileSize),
        tileSize,
        durationMs
    )
    companion object {
        fun getFrames(bitmap: Bitmap, numColTiles: Int, tileSize: Int): List<Bitmap> =
            (0 until numColTiles).map { index ->
                val x = index * tileSize
                val y = 0
                Bitmap.createBitmap(
                    bitmap,
                    x,
                    y,
                    tileSize,
                    tileSize,
                )
            }
    }
}
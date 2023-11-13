package com.andreromano.invaders.scenes.editor

import android.graphics.Canvas
import com.andreromano.invaders.ClickListenerRegistry
import com.andreromano.invaders.Game
import com.andreromano.invaders.Scene
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.ViewEvent
import com.andreromano.invaders.scenes.intro.ButtonEntity
import com.andreromano.invaders.ColumnEntity
import com.andreromano.invaders.scenes.level.entities.PathEntity

object EditorState {
    var mapTileWidth = 0
    var mapTileHeight = 0

}

class EditorScene(
    game: Game
) : Scene(
    game = game,
) {

    private var screenWidth = game.width
    private var screenHeight = game.height

    private val incTileWidthButton = ButtonEntity(this, "+ row" , Vec2F.ZERO, 20f, {
        // EditorState.mapTileWidth++
        gameboardEntity.tileWidth++
        gameboardEntity.invalidate()
    })
    private val decTileWidthButton = ButtonEntity(this, "- row" , Vec2F.ZERO, 20f, {
        // EditorState.mapTileWidth = ( EditorState.mapTileWidth - 1 ).coerceAtLeast(1)
        gameboardEntity.tileWidth--
        gameboardEntity.invalidate()
    })
    private val incTileHeightButton = ButtonEntity(this, "+ col" , Vec2F.ZERO, 20f, {
        // EditorState.mapTileHeight++
        gameboardEntity.tileHeight++
        gameboardEntity.invalidate()
    })
    private val decTileHeightButton = ButtonEntity(this, "- col" , Vec2F.ZERO, 20f, {
        // EditorState.mapTileHeight = ( EditorState.mapTileHeight - 1 ).coerceAtLeast(1)
        gameboardEntity.tileHeight--
        gameboardEntity.invalidate()
    })
    private val tileSizeColumn = ColumnEntity(
        Vec2F(x = screenWidth - 200f, y = 0f),
        200, ColumnEntity.WRAP_CONTENT,
        incTileWidthButton,
        decTileWidthButton,
        incTileHeightButton,
        decTileHeightButton,
    )
    private val gameboardEntity = GameboardEntity(this, Vec2F.ZERO, screenWidth, screenHeight)

    override fun updateAndRender(canvas: Canvas, deltaTime: Int) {
        gameboardEntity.entitiesMap.forEachIndexed { tileY, row ->
            row.forEachIndexed { tileX, entity ->
                if (entity == null) {
                    val x = gameboardEntity.getScreenXFromTileX(tileX).toFloat()
                    val y = gameboardEntity.getScreenYFromTileY(tileY).toFloat()
                    val pos = Vec2F(x, y)
                    gameboardEntity.entitiesMap[tileY][tileX] = PathEntity(
                        pos, tileX, tileY, gameboardEntity.entityWidth, gameboardEntity.entityHeight
                    )
                }
            }
        }
        gameboardEntity.update(deltaTime)
        gameboardEntity.render(canvas)
        tileSizeColumn.update(deltaTime)
        tileSizeColumn.render(canvas)
    }

    override fun sceneSizeChanged() {
        screenWidth = game.width
        screenHeight = game.height
        tileSizeColumn.pos = tileSizeColumn.pos.copy(x = screenWidth.toFloat() - tileSizeColumn.width)
        gameboardEntity.width = screenWidth
        gameboardEntity.height = screenHeight
        gameboardEntity.invalidate()
    }

    override fun onViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ScreenClicked -> {
                ClickListenerRegistry.onScreenClicked(event.x, event.y)
            }
        }
    }
}
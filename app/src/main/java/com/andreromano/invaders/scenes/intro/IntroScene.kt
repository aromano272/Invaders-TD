package com.andreromano.invaders.scenes.intro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.andreromano.invaders.animation.AnimatedEntity
import com.andreromano.invaders.ClickListenerRegistry
import com.andreromano.invaders.ColumnEntity
import com.andreromano.invaders.Game
import com.andreromano.invaders.Persistence
import com.andreromano.invaders.PosMode
import com.andreromano.invaders.Scene
import com.andreromano.invaders.UiEntity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.ViewEvent
import com.andreromano.invaders.extensions.toPx
import com.andreromano.invaders.scenes.editor.EditorScene
import com.andreromano.invaders.scenes.level.LevelScene
import com.andreromano.invaders.scenes.level.SaveableLevelState

class IntroScene(
    game: Game,
) : Scene(
    game = game
) {

    private var screenWidth = game.width
    private var screenHeight = game.height

    private val savedGame: SaveableLevelState? = Persistence.load()
    private val resumeButton = if (savedGame != null) {
        ButtonEntity(this, "Resume", Vec2F(0f, 0f), 40f, {
            game.changeScene(LevelScene(game, savedGame))
        })
    } else {
        null
    }
    private val newGameButton = ButtonEntity(this, "New Game", Vec2F(0f, 0f), 40f, {
        game.changeScene(LevelScene(game, null))
    })
    private val editorButton = ButtonEntity(this, "Editor", Vec2F(0f, 0f), 40f, {
        game.changeScene(EditorScene(game))
    })
    private val column = ColumnEntity(
        Vec2F(0f, 0f),
        1000, screenHeight,
        listOfNotNull(
            resumeButton,
            newGameButton,
            editorButton
        )
    )

    override fun updateAndRender(canvas: Canvas, deltaTime: Int) {
        column.update(deltaTime)
        column.render(canvas)
    }

    override fun sceneSizeChanged() {
        screenWidth = game.width
        screenHeight = game.height
        column.height = game.height
    }

    override fun onViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ScreenClicked -> {
                ClickListenerRegistry.onScreenClicked(event.x, event.y)
            }
        }
    }
}

class ButtonEntity(
    scene: Scene,
    val text: String,
    pos: Vec2F,
    private val textSize: Float,
    private val onEntityClick: () -> Unit,
) : UiEntity(
    scene = scene,
    pos = pos,
    width = -1,
    height = -1,
    posMode = PosMode.TL,
) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        textSize = this@ButtonEntity.textSize.toPx
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }

    private val debugPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.MAGENTA
    }

    init {
        val textWidth = paint.measureText(text)
        val textHeight = paint.fontMetrics.run {
            descent - ascent
        }
        width = textWidth.toInt()
        height = textHeight.toInt()
    }

    override fun onClick(x: Float, y: Float): Boolean {
        onEntityClick()
        return true
    }

    override fun update(deltaTime: Int) {
        super.update(deltaTime)
    }

    override fun render(canvas: Canvas) {
        canvas.drawText(text, hitbox.left, hitbox.bottom, paint)
        canvas.drawRect(hitbox, debugPaint)
    }
}
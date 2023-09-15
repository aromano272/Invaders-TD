package com.andreromano.invaders.scenes.intro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.andreromano.invaders.ClickListenerRegistry
import com.andreromano.invaders.Entity
import com.andreromano.invaders.Game
import com.andreromano.invaders.Persistence
import com.andreromano.invaders.PosMode
import com.andreromano.invaders.Scene
import com.andreromano.invaders.UiEntity
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.ViewEvent
import com.andreromano.invaders.extensions.toPx
import com.andreromano.invaders.scenes.intro.ColumnEntity.Companion.WRAP_CONTENT
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
        ButtonEntity(this, "Resume", Vec2F(0f, 0f), {
            game.changeScene(LevelScene(game, savedGame))
        })
    } else {
        null
    }
    private val newGameButton = ButtonEntity(this, "New Game", Vec2F(0f, 0f), {
        game.changeScene(LevelScene(game, null))
    })
    private val column = ColumnEntity(
        Vec2F(0f, 0f),
        1000, screenHeight,
        listOfNotNull(
            resumeButton,
            newGameButton
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

class ColumnEntity(
    pos: Vec2F,
    width: Int,
    height: Int,
    private val children: List<Entity>,
) : Entity(
    pos = pos,
    width = width,
    height = height,
    posMode = PosMode.TL,
) {

    private val lastChildrenHeight: MutableList<Int> = mutableListOf()

    private fun layoutChildren() {
//        if (children.filterIndexed { index, entity -> entity.height != lastChildrenHeight.getOrNull(index) }.isEmpty()) return

        var currY = if (height == WRAP_CONTENT) {
            pos.y
        } else {
            val childrenHeight = children.sumOf { it.height }
            pos.y + (height - childrenHeight) / 2
        }
        children.forEach {
            it.pos.y = currY
            it.pos.x = pos.x

            currY += it.height
        }
    }

    override fun update(deltaTime: Int) {
        children.forEach {
            it.update(deltaTime)
        }
    }

    override fun render(canvas: Canvas) {
        layoutChildren()
        children.forEach {
            it.render(canvas)
        }
    }

    companion object {
        const val WRAP_CONTENT = -1
    }
}

class ButtonEntity(
    scene: Scene,
    val text: String,
    pos: Vec2F,
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
        textSize = 40f.toPx
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

    override fun onClick(): Boolean {
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
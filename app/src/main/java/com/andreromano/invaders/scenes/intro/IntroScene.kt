package com.andreromano.invaders.scenes.intro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.andreromano.invaders.ClickListenerRegistry
import com.andreromano.invaders.Entity
import com.andreromano.invaders.GameState
import com.andreromano.invaders.Persistence
import com.andreromano.invaders.PosMode
import com.andreromano.invaders.Scene
import com.andreromano.invaders.Vec2F
import com.andreromano.invaders.ViewEvent
import com.andreromano.invaders.extensions.toPx
import com.andreromano.invaders.scenes.intro.ColumnEntity.Companion.WRAP_CONTENT
import com.andreromano.invaders.scenes.level.LevelScene
import com.andreromano.invaders.scenes.level.levelState
import com.andreromano.invaders.scenes.level.LevelState
import com.andreromano.invaders.scenes.level.SaveableLevelState

class IntroScene : Scene {

    private var sceneWidth = 0
    private var sceneHeight = 0

    private val savedGame: SaveableLevelState? = Persistence.load()
    private val resumeButton = if (savedGame != null) {
        ButtonEntity("Resume", Vec2F(0f, 0f)) {
            GameState.activeScene = LevelScene(savedGame)
            GameState.activeScene.sceneSizeChanged(sceneWidth, sceneHeight)
            true
        }
    } else {
        null
    }
    private val newGameButton = ButtonEntity("New Game", Vec2F(0f, 0f)) {
        GameState.activeScene = LevelScene(null)
        GameState.activeScene.sceneSizeChanged(sceneWidth, sceneHeight)
        true
    }
    private val column = ColumnEntity(
        Vec2F(0f, 0f),
        1000, WRAP_CONTENT,
        listOfNotNull(
            resumeButton,
            newGameButton
        )
    )

    override fun updateAndRender(canvas: Canvas, deltaTime: Int) {
        column.update(deltaTime)
        column.render(canvas)
    }

    override fun sceneSizeChanged(w: Int, h: Int) {
        sceneWidth = w
        sceneHeight = h
        column.height = h
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
        children.forEachIndexed { index, it ->
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
    val text: String,
    pos: Vec2F,
    onClick: () -> Boolean,
) : Entity(
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
        ClickListenerRegistry.register(this, onClick)
        val textWidth = paint.measureText(text)
        val textHeight = paint.fontMetrics.run {
            descent - ascent
        }
        width = textWidth.toInt()
        height = textHeight.toInt()
    }

    override fun update(deltaTime: Int) {

    }

    override fun render(canvas: Canvas) {
        canvas.drawText(text, hitbox.left, hitbox.bottom, paint)
        canvas.drawRect(hitbox, debugPaint)
    }
}
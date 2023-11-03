package com.andreromano.invaders

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.andreromano.invaders.extensions.round
import com.andreromano.invaders.extensions.toPx
import com.andreromano.invaders.scenes.intro.IntroScene
import java.util.*


class RenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : SurfaceView(context, attrs, defStyleAttr), Runnable {

    init {
        Persistence.initialise(context)
        TileAtlas.initialise(context)
    }

    private var isRunning: Boolean = false
    private lateinit var thread: Thread

    private val pendingEvents = mutableListOf<ViewEvent>()

    private val game: Game = object : Game {
        override var width: Int = 0
        override var height: Int = 0
        override var activeScene: Scene = IntroScene(this)

        override fun changeScene(scene: Scene) {
            ClickListenerRegistry.remove(activeScene)
            activeScene = scene
        }
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 16f.toPx
        color = Color.WHITE
    }

    private val redPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val greenPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    override fun run() {
        while (isRunning) {
            if (!holder.surface.isValid) return
            val canvas = holder.lockHardwareCanvas() ?: continue

            val currFrameMs = System.currentTimeMillis()
            val lastFrameMs = lastFrameMs
            val deltaTime = (currFrameMs - lastFrameMs)

            val rect = Rect(0, 0, width, height)

            canvas.drawRect(rect, paint)
            computeFrameStartNano = System.nanoTime()
            if (pendingEvents.isNotEmpty()) {
                pendingEvents.forEach { event -> game.activeScene.onViewEvent(event) }
                pendingEvents.clear()
            }
            game.activeScene.updateAndRender(canvas, deltaTime.toInt())
            computeFrameEndNano = System.nanoTime()

            drawFps(canvas)

            holder.unlockCanvasAndPost(canvas)
            frameCount++
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        game.width = w
        game.height = h
        game.activeScene.sceneSizeChanged()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != ACTION_DOWN) return false

        pendingEvents += ViewEvent.ScreenClicked(event.x, event.y)

        return true
    }

    private var frameCount: Long = 0
    private var last100FrameTimes: LinkedList<Long> = LinkedList()
    private var last100ComputeFrameTimes: LinkedList<Long> = LinkedList()
    private var lastFrameMs: Long = System.currentTimeMillis()
    private var computeFrameStartNano: Long = System.nanoTime()
    private var computeFrameEndNano: Long = System.nanoTime()
    private var medianFrameTime: Long = 0
    private var minFrameTime: Long = 0
    private var maxFrameTime: Long = 0
    private var medianComputeFrameTime: Long = 0

    private fun drawFps(canvas: Canvas) {
        val currFrameMs = System.currentTimeMillis()
        val lastFrameMs = lastFrameMs
        this.lastFrameMs = currFrameMs
        val currFrameTime = (currFrameMs - lastFrameMs)
        val currComputeFrameTime = (computeFrameEndNano - computeFrameStartNano) / 1_000

        if (last100FrameTimes.size == 100) last100FrameTimes.removeFirst()
        if (last100ComputeFrameTimes.size == 100) last100ComputeFrameTimes.removeFirst()
        last100FrameTimes.addLast(currFrameTime)
        last100ComputeFrameTimes.addLast(currComputeFrameTime)

        if (frameCount % 60 == 0L) {
            val sortedFrameTimes = last100FrameTimes.sorted()
            val sortedComputeFrameTimes = last100ComputeFrameTimes.sorted()
            medianFrameTime = sortedFrameTimes.getOrNull(50) ?: 0
            minFrameTime = sortedFrameTimes.firstOrNull() ?: 0
            maxFrameTime = sortedFrameTimes.lastOrNull() ?: 0
            medianComputeFrameTime = sortedComputeFrameTimes.getOrNull(50) ?: 0
        }

        val fps = (1000.0 / (medianFrameTime)).round(1)

        canvas.drawText("FPS: $fps", 50f, 50f, textPaint)
        canvas.drawText("Min FT: $minFrameTime", 50f, 50f + textPaint.textSize, textPaint)
        canvas.drawText("Max FT: $maxFrameTime", 50f, 50f + textPaint.textSize * 2, textPaint)
        canvas.drawText("Compute FT: ${medianComputeFrameTime / 1_000f}", 50f, 50f + textPaint.textSize * 3, textPaint)
    }



    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                resume()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                pause()
            }
        })
    }

    /**
     * Called by MainActivity.onPause() to stop the thread.
     */
    private fun pause() {
        isRunning = false
        try {
            // Stop the thread == rejoin the main thread.
            thread.join()
        } catch (e: InterruptedException) {
        }
    }

    /**
     * Called by MainActivity.onResume() to start a thread.
     */
    private fun resume() {
        isRunning = true
        thread = Thread(this)
        thread.start()
    }

    fun onViewEvent(viewEvent: ViewEvent) {
        game.activeScene.onViewEvent(viewEvent)
    }
}
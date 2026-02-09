package com.jazz.drawinggame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.*

class GuidedDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val userCanvas = Canvas()
    private var userBitmap: Bitmap? = null
    
    private val guidePaint = Paint().apply {
        color = Color.parseColor("#806B21A8")  // Semi-transparent purple
        style = Paint.Style.STROKE
        strokeWidth = 8f
        pathEffect = DashPathEffect(floatArrayOf(20f, 15f), 0f)  // Dashed line
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    
    private val userPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    
    private var guideDrawer: ((Canvas, Paint, Int, Int) -> Unit)? = null
    private var currentPath = Path()
    private var currentX = 0f
    private var currentY = 0f
    
    private var lastDrawTime = 0L
    private val pauseDelay = 3000L  // 3 seconds
    var onDrawingPaused: (() -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var pauseJob: Job? = null
    
    var currentColor = Color.BLACK
        set(value) {
            field = value
            userPaint.color = value
        }
    
    var brushSize = 12f
        set(value) {
            field = value
            userPaint.strokeWidth = value
        }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        userBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        userCanvas.setBitmap(userBitmap)
        userCanvas.drawColor(Color.WHITE)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw white background
        canvas.drawColor(Color.WHITE)
        
        // Draw guide (dashed overlay) - pass width and height
        guideDrawer?.invoke(canvas, guidePaint, width, height)
        
        // Draw user's drawing
        userBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        
        // Draw current stroke
        canvas.drawPath(currentPath, userPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
                currentX = x
                currentY = y
                cancelPauseTimer()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                currentX = x
                currentY = y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                userCanvas.drawPath(currentPath, userPaint)
                currentPath.reset()
                invalidate()
                startPauseTimer()
            }
        }
        return true
    }
    
    private fun startPauseTimer() {
        lastDrawTime = System.currentTimeMillis()
        pauseJob?.cancel()
        pauseJob = scope.launch {
            delay(pauseDelay)
            if (System.currentTimeMillis() - lastDrawTime >= pauseDelay) {
                onDrawingPaused?.invoke()
            }
        }
    }
    
    private fun cancelPauseTimer() {
        pauseJob?.cancel()
    }
    
    fun setGuideDrawer(drawer: (Canvas, Paint, Int, Int) -> Unit) {
        guideDrawer = drawer
        invalidate()
    }
    
    fun hideGuide() {
        guideDrawer = null
        invalidate()
    }
    
    fun clearCanvas() {
        userBitmap?.let {
            userCanvas.drawColor(Color.WHITE)
        }
        currentPath.reset()
        invalidate()
    }
    
    fun getCanvasBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        userBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        return bitmap
    }
}

package com.jazz.drawinggame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.*

class GuidedDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val TAG = "GuidedDrawingView"
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
        Log.d(TAG, "onSizeChanged: w=$w h=$h oldw=$oldw oldh=$oldh")
        
        // Save old bitmap if it exists
        val oldBitmap = userBitmap
        val hadBitmap = oldBitmap != null
        Log.d(TAG, "onSizeChanged: hadBitmap=$hadBitmap oldSize=${oldBitmap?.width}x${oldBitmap?.height}")
        
        // Create new bitmap
        userBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        userCanvas.setBitmap(userBitmap)
        userCanvas.drawColor(Color.WHITE)
        Log.d(TAG, "onSizeChanged: Created new bitmap ${w}x${h}")
        
        // Restore old drawing if size matches
        if (oldBitmap != null && oldBitmap.width == w && oldBitmap.height == h) {
            Log.d(TAG, "onSizeChanged: Restoring old bitmap (sizes match)")
            userCanvas.drawBitmap(oldBitmap, 0f, 0f, null)
            oldBitmap.recycle()
        } else if (oldBitmap != null) {
            Log.w(TAG, "onSizeChanged: NOT restoring - size mismatch! old=${oldBitmap.width}x${oldBitmap.height} new=${w}x${h}")
            oldBitmap.recycle()
        } else {
            Log.d(TAG, "onSizeChanged: No old bitmap to restore")
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw white background
        canvas.drawColor(Color.WHITE)
        
        // Draw user's drawing FIRST
        userBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        
        // Draw current stroke
        canvas.drawPath(currentPath, userPaint)
        
        // Draw guide LAST (on top) - pass width and height
        guideDrawer?.invoke(canvas, guidePaint, width, height)
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
    
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        val visStr = when(visibility) {
            View.VISIBLE -> "VISIBLE"
            View.INVISIBLE -> "INVISIBLE"
            View.GONE -> "GONE"
            else -> "UNKNOWN"
        }
        Log.d(TAG, "onVisibilityChanged: visibility=$visStr hasBitmap=${userBitmap != null}")
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow: hasBitmap=${userBitmap != null}")
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow: hasBitmap=${userBitmap != null}")
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
        Log.d(TAG, "setGuideDrawer: Guide set")
        invalidate()
    }
    
    fun hideGuide() {
        guideDrawer = null
        Log.d(TAG, "hideGuide: Guide hidden")
        invalidate()
    }
    
    fun clearCanvas() {
        Log.d(TAG, "clearCanvas: Clearing canvas")
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
    
    fun saveBitmapState(): Bitmap? {
        Log.d(TAG, "saveBitmapState: Saving current bitmap")
        return userBitmap?.copy(Bitmap.Config.ARGB_8888, false)
    }
    
    fun restoreBitmapState(savedBitmap: Bitmap?) {
        if (savedBitmap != null) {
            Log.d(TAG, "restoreBitmapState: Restoring saved bitmap ${savedBitmap.width}x${savedBitmap.height}")
            userBitmap?.let {
                userCanvas.drawBitmap(savedBitmap, 0f, 0f, null)
            }
            invalidate()
        } else {
            Log.d(TAG, "restoreBitmapState: No bitmap to restore")
        }
    }
}

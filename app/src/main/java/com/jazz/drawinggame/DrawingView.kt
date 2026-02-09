package com.jazz.drawinggame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 30f
    }

    private val path = Path()
    private val drawingBitmap by lazy { Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) }
    private val bitmapCanvas by lazy { Canvas(drawingBitmap) }
    
    private var rainbowMode = false
    private var hue = 0f
    private var currentColor = Color.parseColor("#FF6B6B")

    init {
        paint.color = currentColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmapCanvas.drawColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(drawingBitmap, 0f, 0f, null)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                if (rainbowMode) {
                    hue = (hue + 10) % 360
                    paint.color = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                if (rainbowMode) {
                    hue = (hue + 2) % 360
                    paint.color = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                }
            }
            MotionEvent.ACTION_UP -> {
                bitmapCanvas.drawPath(path, paint)
                path.reset()
            }
        }

        invalidate()
        return true
    }

    fun setColor(color: Int) {
        rainbowMode = false
        currentColor = color
        paint.color = color
    }

    fun setRainbowMode(enabled: Boolean) {
        rainbowMode = enabled
        if (enabled) {
            hue = 0f
        }
    }

    fun setBrushSize(size: Float) {
        paint.strokeWidth = size
    }

    fun clearCanvas() {
        path.reset()
        drawingBitmap.eraseColor(Color.WHITE)
        invalidate()
    }

    fun getBitmap(): Bitmap {
        return drawingBitmap.copy(Bitmap.Config.ARGB_8888, false)
    }
    
    fun loadBitmap(bitmap: Bitmap) {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        bitmapCanvas.drawBitmap(scaledBitmap, 0f, 0f, null)
        path.reset()
        invalidate()
    }
}

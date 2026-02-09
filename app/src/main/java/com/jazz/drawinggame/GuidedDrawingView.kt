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
        color = Color.parseColor("#80000000")  // Semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)  // Dashed line
    }
    
    private val userPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    
    private var guidePath: Path? = null
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
    
    var brushSize = 8f
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
        
        // Draw guide path (dashed overlay)
        guidePath?.let {
            canvas.drawPath(it, guidePaint)
        }
        
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
    
    fun setGuidePath(svgPathData: String) {
        guidePath = parseSvgPath(svgPathData)
        invalidate()
    }
    
    fun hideGuide() {
        guidePath = null
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
    
    private fun parseSvgPath(pathData: String): Path {
        val path = Path()
        val commands = pathData.trim().split("\\s+".toRegex())
        var i = 0
        var currentX = 0f
        var currentY = 0f
        var lastCommand = ""
        
        while (i < commands.size) {
            val cmd = commands[i]
            
            when {
                cmd == "M" -> {
                    i++
                    val parts = commands[i].split(",")
                    currentX = parts[0].toFloat()
                    currentY = parts[1].toFloat()
                    path.moveTo(currentX, currentY)
                    lastCommand = "M"
                }
                cmd == "L" -> {
                    i++
                    val parts = commands[i].split(",")
                    currentX = parts[0].toFloat()
                    currentY = parts[1].toFloat()
                    path.lineTo(currentX, currentY)
                    lastCommand = "L"
                }
                cmd == "Q" -> {
                    i++
                    val cp = commands[i].split(",")
                    i++
                    val end = commands[i].split(",")
                    currentX = end[0].toFloat()
                    currentY = end[1].toFloat()
                    path.quadTo(cp[0].toFloat(), cp[1].toFloat(), currentX, currentY)
                    lastCommand = "Q"
                }
                cmd == "Z" -> {
                    path.close()
                    lastCommand = "Z"
                }
                cmd == "m" -> {
                    i++
                    val parts = commands[i].split(",")
                    currentX += parts[0].toFloat()
                    currentY += parts[1].toFloat()
                    path.moveTo(currentX, currentY)
                    lastCommand = "m"
                }
                cmd == "a" -> {
                    // Simplified arc handling - just approximate with lines
                    i += 3  // Skip rx, ry, flags
                    val end = commands[i].split(",")
                    currentX = end[0].toFloat()
                    currentY = end[1].toFloat()
                    path.lineTo(currentX, currentY)
                    lastCommand = "a"
                }
                else -> {
                    // Try to parse as coordinate continuation
                    try {
                        val parts = cmd.split(",")
                        if (parts.size == 2) {
                            currentX = parts[0].toFloat()
                            currentY = parts[1].toFloat()
                            when (lastCommand) {
                                "L", "M" -> path.lineTo(currentX, currentY)
                            }
                        }
                    } catch (e: Exception) {
                        // Skip invalid commands
                    }
                }
            }
            i++
        }
        
        return path
    }
    
}

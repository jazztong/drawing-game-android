package com.jazz.drawinggame

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class DrawingStep(
    val instruction: String,
    val drawGuide: (android.graphics.Canvas, android.graphics.Paint, Int, Int) -> Unit
)

data class DrawingTemplate(
    val name: String,
    val emoji: String,
    val steps: List<DrawingStep>
)

class GuidedDrawingActivity : AppCompatActivity() {
    private lateinit var drawingView: GuidedDrawingView
    private lateinit var instructionText: TextView
    private lateinit var nextButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var encouragementText: TextView
    
    private val templates = listOf(
        DrawingTemplate(
            name = "Cat",
            emoji = "🐱",
            steps = listOf(
                DrawingStep("Draw a big circle for the head") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val cy = h * 0.3f
                    val radius = w * 0.15f
                    canvas.drawCircle(cx, cy, radius, paint)
                },
                DrawingStep("Add two triangle ears on top") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val cy = h * 0.3f
                    val size = w * 0.08f
                    
                    val leftEar = android.graphics.Path().apply {
                        moveTo(cx - size * 1.5f, cy - size * 1.2f)
                        lineTo(cx - size * 0.8f, cy - size * 2.5f)
                        lineTo(cx - size * 0.2f, cy - size * 0.8f)
                        close()
                    }
                    val rightEar = android.graphics.Path().apply {
                        moveTo(cx + size * 0.2f, cy - size * 0.8f)
                        lineTo(cx + size * 0.8f, cy - size * 2.5f)
                        lineTo(cx + size * 1.5f, cy - size * 1.2f)
                        close()
                    }
                    canvas.drawPath(leftEar, paint)
                    canvas.drawPath(rightEar, paint)
                },
                DrawingStep("Draw two big eyes") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val cy = h * 0.3f
                    val eyeSize = w * 0.03f
                    canvas.drawCircle(cx - w * 0.05f, cy, eyeSize, paint)
                    canvas.drawCircle(cx + w * 0.05f, cy, eyeSize, paint)
                },
                DrawingStep("Add a little nose") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val cy = h * 0.3f
                    val nose = android.graphics.Path().apply {
                        moveTo(cx, cy + w * 0.05f)
                        lineTo(cx - w * 0.02f, cy + w * 0.08f)
                        lineTo(cx + w * 0.02f, cy + w * 0.08f)
                        close()
                    }
                    canvas.drawPath(nose, paint)
                },
                DrawingStep("Draw whiskers on both sides") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val cy = h * 0.3f
                    // Left whiskers
                    canvas.drawLine(cx - w * 0.18f, cy + w * 0.05f, cx - w * 0.28f, cy + w * 0.03f, paint)
                    canvas.drawLine(cx - w * 0.18f, cy + w * 0.09f, cx - w * 0.28f, cy + w * 0.09f, paint)
                    // Right whiskers
                    canvas.drawLine(cx + w * 0.18f, cy + w * 0.03f, cx + w * 0.28f, cy + w * 0.05f, paint)
                    canvas.drawLine(cx + w * 0.18f, cy + w * 0.09f, cx + w * 0.28f, cy + w * 0.09f, paint)
                },
                DrawingStep("Draw a round body below") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val bodyTop = h * 0.45f
                    val bodyBottom = h * 0.7f
                    canvas.drawOval(cx - w * 0.2f, bodyTop, cx + w * 0.2f, bodyBottom, paint)
                },
                DrawingStep("Add a curvy tail") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val tail = android.graphics.Path().apply {
                        moveTo(cx + w * 0.2f, h * 0.6f)
                        cubicTo(cx + w * 0.3f, h * 0.6f, cx + w * 0.35f, h * 0.68f, cx + w * 0.38f, h * 0.75f)
                    }
                    canvas.drawPath(tail, paint)
                }
            )
        ),
        DrawingTemplate(
            name = "House",
            emoji = "🏠",
            steps = listOf(
                DrawingStep("Draw a big square for the house") { canvas, paint, w, h ->
                    val left = w * 0.25f
                    val top = h * 0.4f
                    val right = w * 0.75f
                    val bottom = h * 0.8f
                    canvas.drawRect(left, top, right, bottom, paint)
                },
                DrawingStep("Add a triangle roof on top") { canvas, paint, w, h ->
                    val roof = android.graphics.Path().apply {
                        moveTo(w * 0.2f, h * 0.4f)
                        lineTo(w * 0.5f, h * 0.2f)
                        lineTo(w * 0.8f, h * 0.4f)
                        close()
                    }
                    canvas.drawPath(roof, paint)
                },
                DrawingStep("Draw a door") { canvas, paint, w, h ->
                    val cx = w / 2f
                    canvas.drawRect(cx - w * 0.08f, h * 0.6f, cx + w * 0.08f, h * 0.8f, paint)
                },
                DrawingStep("Add two windows") { canvas, paint, w, h ->
                    // Left window
                    canvas.drawRect(w * 0.3f, h * 0.48f, w * 0.42f, h * 0.58f, paint)
                    // Right window
                    canvas.drawRect(w * 0.58f, h * 0.48f, w * 0.7f, h * 0.58f, paint)
                },
                DrawingStep("Draw a chimney on the roof") { canvas, paint, w, h ->
                    canvas.drawRect(w * 0.62f, h * 0.25f, w * 0.7f, h * 0.35f, paint)
                }
            )
        ),
        DrawingTemplate(
            name = "Flower",
            emoji = "🌸",
            steps = listOf(
                DrawingStep("Draw a circle in the middle") { canvas, paint, w, h ->
                    canvas.drawCircle(w / 2f, h * 0.35f, w * 0.06f, paint)
                },
                DrawingStep("Add 5 petals around it") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val cy = h * 0.35f
                    val petal = w * 0.05f
                    // Top
                    canvas.drawCircle(cx, cy - w * 0.1f, petal, paint)
                    // Right
                    canvas.drawCircle(cx + w * 0.08f, cy - w * 0.03f, petal, paint)
                    // Bottom right
                    canvas.drawCircle(cx + w * 0.05f, cy + w * 0.07f, petal, paint)
                    // Bottom left
                    canvas.drawCircle(cx - w * 0.05f, cy + w * 0.07f, petal, paint)
                    // Left
                    canvas.drawCircle(cx - w * 0.08f, cy - w * 0.03f, petal, paint)
                },
                DrawingStep("Draw a long stem going down") { canvas, paint, w, h ->
                    canvas.drawLine(w / 2f, h * 0.41f, w / 2f, h * 0.8f, paint)
                },
                DrawingStep("Add two leaves on the stem") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val leftLeaf = android.graphics.Path().apply {
                        moveTo(cx, h * 0.55f)
                        quadTo(cx - w * 0.08f, h * 0.58f, cx - w * 0.05f, h * 0.65f)
                        lineTo(cx, h * 0.62f)
                        close()
                    }
                    val rightLeaf = android.graphics.Path().apply {
                        moveTo(cx, h * 0.68f)
                        quadTo(cx + w * 0.08f, h * 0.71f, cx + w * 0.05f, h * 0.78f)
                        lineTo(cx, h * 0.75f)
                        close()
                    }
                    canvas.drawPath(leftLeaf, paint)
                    canvas.drawPath(rightLeaf, paint)
                }
            )
        ),
        DrawingTemplate(
            name = "Sun",
            emoji = "☀️",
            steps = listOf(
                DrawingStep("Draw a big circle for the sun") { canvas, paint, w, h ->
                    canvas.drawCircle(w / 2f, h * 0.35f, w * 0.12f, paint)
                },
                DrawingStep("Add rays all around (8 lines)") { canvas, paint, w, h ->
                    val cx = w / 2f
                    val cy = h * 0.35f
                    val inner = w * 0.13f
                    val outer = w * 0.18f
                    // Top
                    canvas.drawLine(cx, cy - inner, cx, cy - outer, paint)
                    // Bottom
                    canvas.drawLine(cx, cy + inner, cx, cy + outer, paint)
                    // Left
                    canvas.drawLine(cx - inner, cy, cx - outer, cy, paint)
                    // Right
                    canvas.drawLine(cx + inner, cy, cx + outer, cy, paint)
                    // Diagonals
                    val diag = inner * 0.707f
                    val diagOut = outer * 0.707f
                    canvas.drawLine(cx - diag, cy - diag, cx - diagOut, cy - diagOut, paint)
                    canvas.drawLine(cx + diag, cy - diag, cx + diagOut, cy - diagOut, paint)
                    canvas.drawLine(cx - diag, cy + diag, cx - diagOut, cy + diagOut, paint)
                    canvas.drawLine(cx + diag, cy + diag, cx + diagOut, cy + diagOut, paint)
                }
            )
        ),
        DrawingTemplate(
            name = "Fish",
            emoji = "🐟",
            steps = listOf(
                DrawingStep("Draw an oval body") { canvas, paint, w, h ->
                    canvas.drawOval(w * 0.3f, h * 0.3f, w * 0.7f, h * 0.5f, paint)
                },
                DrawingStep("Add a triangle tail on the left") { canvas, paint, w, h ->
                    val tail = android.graphics.Path().apply {
                        moveTo(w * 0.3f, h * 0.4f)
                        lineTo(w * 0.18f, h * 0.33f)
                        lineTo(w * 0.18f, h * 0.47f)
                        close()
                    }
                    canvas.drawPath(tail, paint)
                },
                DrawingStep("Draw a fin on top") { canvas, paint, w, h ->
                    val fin = android.graphics.Path().apply {
                        moveTo(w * 0.48f, h * 0.3f)
                        lineTo(w * 0.5f, h * 0.22f)
                        lineTo(w * 0.52f, h * 0.3f)
                        close()
                    }
                    canvas.drawPath(fin, paint)
                },
                DrawingStep("Add a big eye") { canvas, paint, w, h ->
                    canvas.drawCircle(w * 0.6f, h * 0.38f, w * 0.03f, paint)
                },
                DrawingStep("Draw a smiling mouth") { canvas, paint, w, h ->
                    val mouth = android.graphics.Path().apply {
                        moveTo(w * 0.65f, h * 0.4f)
                        quadTo(w * 0.68f, h * 0.43f, w * 0.65f, h * 0.46f)
                    }
                    canvas.drawPath(mouth, paint)
                }
            )
        )
    )
    
    private var currentTemplate: DrawingTemplate? = null
    private var currentStepIndex = 0
    private val apiKey = "AIzaSyAkIxaFz8O3QIyCclrsD4uO-uFzVmfJcN0"
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guided_drawing)
        
        drawingView = findViewById(R.id.guidedDrawingView)
        instructionText = findViewById(R.id.instructionText)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.progressBar)
        encouragementText = findViewById(R.id.encouragementText)
        
        val templateSpinner = findViewById<Spinner>(R.id.templateSpinner)
        val startButton = findViewById<Button>(R.id.startButton)
        val backButton = findViewById<Button>(R.id.backButton)
        
        // Setup template spinner
        val templateNames = templates.map { "${it.emoji} ${it.name}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, templateNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        templateSpinner.adapter = adapter
        
        startButton.setOnClickListener {
            val selectedIndex = templateSpinner.selectedItemPosition
            startGuidedDrawing(templates[selectedIndex])
        }
        
        nextButton.setOnClickListener {
            moveToNextStep()
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun startGuidedDrawing(template: DrawingTemplate) {
        currentTemplate = template
        currentStepIndex = 0
        drawingView.clearCanvas()
        
        findViewById<LinearLayout>(R.id.templateSelection).visibility = View.GONE
        findViewById<LinearLayout>(R.id.drawingArea).visibility = View.VISIBLE
        
        showCurrentStep()
    }
    
    private fun showCurrentStep() {
        val template = currentTemplate ?: return
        if (currentStepIndex >= template.steps.size) {
            showCompletion()
            return
        }
        
        val step = template.steps[currentStepIndex]
        instructionText.text = "${currentStepIndex + 1}/${template.steps.size}: ${step.instruction}"
        drawingView.setGuideDrawer(step.drawGuide)
        encouragementText.text = ""
        encouragementText.visibility = View.GONE
        
        // Auto-analyze after 3 seconds of no drawing
        drawingView.onDrawingPaused = {
            analyzeProgress()
        }
    }
    
    private fun analyzeProgress() {
        scope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val bitmap = drawingView.getCanvasBitmap()
                val base64Image = bitmapToBase64(bitmap)
                
                val isGoodEnough = callGeminiVision(base64Image)
                
                progressBar.visibility = View.GONE
                
                if (isGoodEnough) {
                    encouragementText.text = getEncouragement()
                    encouragementText.visibility = View.VISIBLE
                    
                    // Auto-advance after 2 seconds
                    scope.launch {
                        delay(2000)
                        moveToNextStep()
                    }
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                e.printStackTrace()
            }
        }
    }
    
    private fun moveToNextStep() {
        currentStepIndex++
        showCurrentStep()
    }
    
    private fun showCompletion() {
        instructionText.text = "🎉 Amazing! You finished the ${currentTemplate?.name}!"
        encouragementText.text = "You're a great artist! ⭐✨"
        encouragementText.visibility = View.VISIBLE
        drawingView.hideGuide()
        nextButton.text = "Draw Another"
        nextButton.setOnClickListener {
            findViewById<LinearLayout>(R.id.drawingArea).visibility = View.GONE
            findViewById<LinearLayout>(R.id.templateSelection).visibility = View.VISIBLE
            nextButton.text = "Next Step"
        }
    }
    
    private suspend fun callGeminiVision(base64Image: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val template = currentTemplate ?: return@withContext false
            val currentStep = template.steps[currentStepIndex]
            
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val prompt = """
                You are analyzing a child's drawing progress. The child is learning to draw a ${template.name}.
                Current step: "${currentStep.instruction}"
                
                Look at the drawing and answer ONLY "YES" or "NO":
                - YES if the child has made a reasonable attempt at this step (doesn't need to be perfect)
                - NO if the canvas is mostly blank or the step hasn't been attempted
                
                Be encouraging - if you see ANY attempt, say YES!
            """.trimIndent()
            
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", "image/png")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
            }
            
            connection.outputStream.write(requestBody.toString().toByteArray())
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)
                val text = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                
                return@withContext text.uppercase().startsWith("YES")
            }
            
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
    
    private fun getEncouragement(): String {
        val messages = listOf(
            "Great job! ⭐",
            "You're doing amazing! 🌟",
            "Perfect! Let's keep going! ✨",
            "Wonderful work! 🎨",
            "You're a natural artist! 🎭",
            "Fantastic! 🌈",
            "Beautiful! 💖",
            "Excellent drawing! 👏",
            "You've got this! 💪",
            "Amazing progress! 🚀"
        )
        return messages.random()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

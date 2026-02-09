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
    val drawGuide: (android.graphics.Canvas, android.graphics.Paint) -> Unit
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
                DrawingStep("Draw a big circle for the head") { canvas, paint ->
                    canvas.drawCircle(500f, 400f, 150f, paint)
                },
                DrawingStep("Add two triangle ears on top") { canvas, paint ->
                    val leftEar = android.graphics.Path().apply {
                        moveTo(380f, 300f)
                        lineTo(420f, 200f)
                        lineTo(460f, 280f)
                        close()
                    }
                    val rightEar = android.graphics.Path().apply {
                        moveTo(540f, 280f)
                        lineTo(580f, 200f)
                        lineTo(620f, 300f)
                        close()
                    }
                    canvas.drawPath(leftEar, paint)
                    canvas.drawPath(rightEar, paint)
                },
                DrawingStep("Draw two big eyes") { canvas, paint ->
                    canvas.drawCircle(450f, 380f, 25f, paint)
                    canvas.drawCircle(550f, 380f, 25f, paint)
                },
                DrawingStep("Add a little nose") { canvas, paint ->
                    val nose = android.graphics.Path().apply {
                        moveTo(500f, 420f)
                        lineTo(490f, 440f)
                        lineTo(510f, 440f)
                        close()
                    }
                    canvas.drawPath(nose, paint)
                },
                DrawingStep("Draw whiskers on both sides") { canvas, paint ->
                    // Left whiskers
                    canvas.drawLine(370f, 420f, 420f, 410f, paint)
                    canvas.drawLine(370f, 440f, 420f, 440f, paint)
                    // Right whiskers
                    canvas.drawLine(580f, 410f, 630f, 420f, paint)
                    canvas.drawLine(580f, 440f, 630f, 440f, paint)
                },
                DrawingStep("Draw a round body below") { canvas, paint ->
                    canvas.drawOval(400f, 530f, 600f, 750f, paint)
                },
                DrawingStep("Add a curvy tail") { canvas, paint ->
                    val tail = android.graphics.Path().apply {
                        moveTo(600f, 650f)
                        cubicTo(650f, 650f, 680f, 700f, 700f, 750f)
                    }
                    canvas.drawPath(tail, paint)
                }
            )
        ),
        DrawingTemplate(
            name = "House",
            emoji = "🏠",
            steps = listOf(
                DrawingStep("Draw a big square for the house") { canvas, paint ->
                    canvas.drawRect(300f, 450f, 700f, 850f, paint)
                },
                DrawingStep("Add a triangle roof on top") { canvas, paint ->
                    val roof = android.graphics.Path().apply {
                        moveTo(250f, 450f)
                        lineTo(500f, 250f)
                        lineTo(750f, 450f)
                        close()
                    }
                    canvas.drawPath(roof, paint)
                },
                DrawingStep("Draw a door") { canvas, paint ->
                    canvas.drawRect(420f, 650f, 580f, 850f, paint)
                },
                DrawingStep("Add two windows") { canvas, paint ->
                    canvas.drawRect(340f, 520f, 450f, 620f, paint)
                    canvas.drawRect(550f, 520f, 660f, 620f, paint)
                },
                DrawingStep("Draw a chimney on the roof") { canvas, paint ->
                    canvas.drawRect(600f, 300f, 680f, 400f, paint)
                }
            )
        ),
        DrawingTemplate(
            name = "Flower",
            emoji = "🌸",
            steps = listOf(
                DrawingStep("Draw a circle in the middle") { canvas, paint ->
                    canvas.drawCircle(500f, 450f, 60f, paint)
                },
                DrawingStep("Add 5 petals around it") { canvas, paint ->
                    // Top
                    canvas.drawCircle(500f, 350f, 50f, paint)
                    // Right
                    canvas.drawCircle(580f, 420f, 50f, paint)
                    // Bottom right
                    canvas.drawCircle(550f, 520f, 50f, paint)
                    // Bottom left
                    canvas.drawCircle(450f, 520f, 50f, paint)
                    // Left
                    canvas.drawCircle(420f, 420f, 50f, paint)
                },
                DrawingStep("Draw a long stem going down") { canvas, paint ->
                    canvas.drawLine(500f, 510f, 500f, 850f, paint)
                },
                DrawingStep("Add two leaves on the stem") { canvas, paint ->
                    val leftLeaf = android.graphics.Path().apply {
                        moveTo(500f, 650f)
                        quadTo(420f, 670f, 450f, 720f)
                        lineTo(500f, 700f)
                        close()
                    }
                    val rightLeaf = android.graphics.Path().apply {
                        moveTo(500f, 730f)
                        quadTo(580f, 750f, 550f, 800f)
                        lineTo(500f, 780f)
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
                DrawingStep("Draw a big circle for the sun") { canvas, paint ->
                    canvas.drawCircle(500f, 450f, 120f, paint)
                },
                DrawingStep("Add rays all around (8 lines)") { canvas, paint ->
                    // Top
                    canvas.drawLine(500f, 300f, 500f, 250f, paint)
                    // Bottom
                    canvas.drawLine(500f, 600f, 500f, 650f, paint)
                    // Left
                    canvas.drawLine(350f, 450f, 300f, 450f, paint)
                    // Right
                    canvas.drawLine(650f, 450f, 700f, 450f, paint)
                    // Diagonals
                    canvas.drawLine(395f, 345f, 360f, 310f, paint)
                    canvas.drawLine(605f, 345f, 640f, 310f, paint)
                    canvas.drawLine(395f, 555f, 360f, 590f, paint)
                    canvas.drawLine(605f, 555f, 640f, 590f, paint)
                }
            )
        ),
        DrawingTemplate(
            name = "Fish",
            emoji = "🐟",
            steps = listOf(
                DrawingStep("Draw an oval body") { canvas, paint ->
                    canvas.drawOval(350f, 350f, 600f, 550f, paint)
                },
                DrawingStep("Add a triangle tail on the left") { canvas, paint ->
                    val tail = android.graphics.Path().apply {
                        moveTo(350f, 450f)
                        lineTo(250f, 380f)
                        lineTo(250f, 520f)
                        close()
                    }
                    canvas.drawPath(tail, paint)
                },
                DrawingStep("Draw a fin on top") { canvas, paint ->
                    val fin = android.graphics.Path().apply {
                        moveTo(470f, 350f)
                        lineTo(490f, 280f)
                        lineTo(510f, 350f)
                        close()
                    }
                    canvas.drawPath(fin, paint)
                },
                DrawingStep("Add a big eye") { canvas, paint ->
                    canvas.drawCircle(520f, 420f, 30f, paint)
                },
                DrawingStep("Draw a smiling mouth") { canvas, paint ->
                    val mouth = android.graphics.Path().apply {
                        moveTo(560f, 450f)
                        quadTo(580f, 470f, 560f, 490f)
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

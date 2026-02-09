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
    val pathData: String  // SVG path data
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
                DrawingStep("Draw a big circle for the head", "M 250,200 m -80,0 a 80,80 0 1,0 160,0 a 80,80 0 1,0 -160,0"),
                DrawingStep("Add two triangle ears on top", "M 200,150 L 180,110 L 220,130 Z M 300,150 L 280,130 L 320,110 Z"),
                DrawingStep("Draw two eyes", "M 230,190 m -10,0 a 10,10 0 1,0 20,0 a 10,10 0 1,0 -20,0 M 270,190 m -10,0 a 10,10 0 1,0 20,0 a 10,10 0 1,0 -20,0"),
                DrawingStep("Add a small nose", "M 250,210 L 245,220 L 255,220 Z"),
                DrawingStep("Draw whiskers", "M 200,210 L 170,210 M 200,220 L 170,220 M 300,210 L 330,210 M 300,220 L 330,220"),
                DrawingStep("Add the body", "M 250,280 m -60,0 a 60,80 0 1,0 120,0 a 60,80 0 1,0 -120,0"),
                DrawingStep("Draw the tail", "M 310,320 Q 350,330 360,370")
            )
        ),
        DrawingTemplate(
            name = "House",
            emoji = "🏠",
            steps = listOf(
                DrawingStep("Draw a square for the house", "M 180,250 L 180,400 L 320,400 L 320,250 Z"),
                DrawingStep("Add a triangle roof", "M 160,250 L 250,150 L 340,250 Z"),
                DrawingStep("Draw a door", "M 220,320 L 220,400 L 270,400 L 270,320 Z"),
                DrawingStep("Add two windows", "M 200,280 L 200,310 L 230,310 L 230,280 Z M 270,280 L 270,310 L 300,310 L 300,280 Z"),
                DrawingStep("Draw a chimney", "M 280,170 L 280,140 L 310,140 L 310,180")
            )
        ),
        DrawingTemplate(
            name = "Flower",
            emoji = "🌸",
            steps = listOf(
                DrawingStep("Draw a circle for the center", "M 250,250 m -30,0 a 30,30 0 1,0 60,0 a 30,30 0 1,0 -60,0"),
                DrawingStep("Add petals around it", "M 250,220 m -25,0 a 25,25 0 1,0 50,0 a 25,25 0 1,0 -50,0 M 280,230 m -25,0 a 25,25 0 1,0 50,0 a 25,25 0 1,0 -50,0 M 290,260 m -25,0 a 25,25 0 1,0 50,0 a 25,25 0 1,0 -50,0 M 280,290 m -25,0 a 25,25 0 1,0 50,0 a 25,25 0 1,0 -50,0 M 250,300 m -25,0 a 25,25 0 1,0 50,0 a 25,25 0 1,0 -50,0 M 220,290 m -25,0 a 25,25 0 1,0 50,0 a 25,25 0 1,0 -50,0 M 210,260 m -25,0 a 25,25 0 1,0 50,0 a 25,25 0 1,0 -50,0 M 220,230 m -25,0 a 25,25 0 1,0 50,0 a 25,25 0 1,0 -50,0"),
                DrawingStep("Draw the stem", "M 250,320 L 250,450"),
                DrawingStep("Add two leaves", "M 250,380 Q 220,390 210,420 M 250,400 Q 280,410 290,440")
            )
        ),
        DrawingTemplate(
            name = "Sun",
            emoji = "☀️",
            steps = listOf(
                DrawingStep("Draw a circle for the sun", "M 250,250 m -60,0 a 60,60 0 1,0 120,0 a 60,60 0 1,0 -120,0"),
                DrawingStep("Add rays all around", "M 250,190 L 250,150 M 250,310 L 250,350 M 310,250 L 350,250 M 190,250 L 150,250 M 290,210 L 320,180 M 210,290 L 180,320 M 290,290 L 320,320 M 210,210 L 180,180")
            )
        ),
        DrawingTemplate(
            name = "Fish",
            emoji = "🐟",
            steps = listOf(
                DrawingStep("Draw an oval body", "M 200,250 Q 230,200 280,250 Q 230,300 200,250"),
                DrawingStep("Add a triangle tail", "M 200,250 L 150,230 L 150,270 Z"),
                DrawingStep("Draw a fin on top", "M 240,220 L 250,180 L 260,220"),
                DrawingStep("Add an eye", "M 270,240 m -8,0 a 8,8 0 1,0 16,0 a 8,8 0 1,0 -16,0"),
                DrawingStep("Draw mouth", "M 290,250 Q 295,255 290,260")
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
        drawingView.setGuidePath(step.pathData)
        encouragementText.text = ""
        
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

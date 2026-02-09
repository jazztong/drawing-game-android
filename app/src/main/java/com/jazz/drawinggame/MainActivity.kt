package com.jazz.drawinggame

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var drawingView: DrawingView
    private val REQUEST_PERMISSION = 1
    
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { loadImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)

        // Color buttons
        findViewById<Button>(R.id.colorRed).setOnClickListener { drawingView.setColor(0xFFFF6B6B.toInt()) }
        findViewById<Button>(R.id.colorOrange).setOnClickListener { drawingView.setColor(0xFFFFA500.toInt()) }
        findViewById<Button>(R.id.colorYellow).setOnClickListener { drawingView.setColor(0xFFFFD93D.toInt()) }
        findViewById<Button>(R.id.colorGreen).setOnClickListener { drawingView.setColor(0xFF6BCF7F.toInt()) }
        findViewById<Button>(R.id.colorBlue).setOnClickListener { drawingView.setColor(0xFF4D96FF.toInt()) }
        findViewById<Button>(R.id.colorPurple).setOnClickListener { drawingView.setColor(0xFF9D4EDD.toInt()) }
        findViewById<Button>(R.id.colorBlack).setOnClickListener { drawingView.setColor(0xFF2D3436.toInt()) }
        findViewById<Button>(R.id.colorWhite).setOnClickListener { drawingView.setColor(0xFFFFFFFF.toInt()) }
        findViewById<Button>(R.id.colorRainbow).setOnClickListener { drawingView.setRainbowMode(true) }

        // Size buttons
        findViewById<Button>(R.id.sizeSmall).setOnClickListener { drawingView.setBrushSize(15f) }
        findViewById<Button>(R.id.sizeMedium).setOnClickListener { drawingView.setBrushSize(30f) }
        findViewById<Button>(R.id.sizeLarge).setOnClickListener { drawingView.setBrushSize(60f) }

        // Action buttons
        findViewById<Button>(R.id.btnClear).setOnClickListener { drawingView.clearCanvas() }
        findViewById<Button>(R.id.btnLoad).setOnClickListener { pickImage.launch("image/*") }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveDrawing() }
    }
    
    private fun loadImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                drawingView.loadBitmap(bitmap)
                Toast.makeText(this, "Drawing loaded! ✅", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveDrawing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
                return
            }
        }
        performSave()
    }

    private fun performSave() {
        try {
            val bitmap = drawingView.getBitmap()
            val filename = "drawing_${System.currentTimeMillis()}.png"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DrawingGame")
                }
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                Toast.makeText(this, "Drawing saved! ✅", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            performSave()
        }
    }
}

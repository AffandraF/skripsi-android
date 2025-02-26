package com.example.skripsi.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Observer
import com.example.skripsi.R
import com.example.skripsi.ui.auth.LoginActivity
import com.example.skripsi.viewmodel.ClassificationViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var buttonCamera: ImageButton
    private lateinit var buttonGallery: ImageButton
    private lateinit var buttonHistory: ImageButton
    private lateinit var imageCapture: ImageCapture
    private val viewModel: ClassificationViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private lateinit var cameraExecutor: ExecutorService
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid.toString()

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }
        Handler(Looper.getMainLooper()).postDelayed({
            splashScreen.setKeepOnScreenCondition { false }
        }, 1500)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        checkAndRequestPermissions()

        buttonCamera = findViewById(R.id.button_camera)
        buttonGallery = findViewById(R.id.button_image)
        buttonHistory = findViewById(R.id.button_history)

        startCamera()
        observeViewModel()
        cameraExecutor = Executors.newSingleThreadExecutor()

        buttonGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }

        buttonCamera.setOnClickListener {
            capturePhoto()
        }

        buttonHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.camera_texture_view).surfaceProvider)
            }

            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(480, 480),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
                    )
                )
                .build()

            imageCapture = ImageCapture.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(createFile()).build()

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(createFile())
                if (savedUri != null) {
                    selectedImageUri = savedUri
                    viewModel.classifyImage(userId, File(savedUri.path!!))
                } else {
                    Toast.makeText(this@MainActivity, "Failed to retrieve image URI.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@MainActivity, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createFile(): File {
        return File(cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsNeeded.isNotEmpty()) {
            requestPermissions(permissionsNeeded.toTypedArray(), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera and storage permissions are required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                try {
                    selectedImageUri = uri
                    val file = uriToFile(uri, this)
                    viewModel.classifyImage(userId, file)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error processing selected image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Image selection canceled.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val myFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(selectedImg)?.use { inputStream ->
            FileOutputStream(myFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }
        return myFile
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    private fun observeViewModel() {
        viewModel.classificationResult.observe(this, Observer { response ->
            response?.let {
                if (selectedImageUri != null) {
                    val resultFragment = ResultFragment.newInstance(
                        it.disease,
                        it.confidence,
                        it.recommendations,
                        selectedImageUri!!
                    )
                    resultFragment.show(supportFragmentManager, resultFragment.tag)
                } else {
                    Toast.makeText(this, "Image URI is not available.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}

package com.example.skripsi.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.skripsi.R
import com.example.skripsi.databinding.ActivityMainBinding
import com.example.skripsi.ui.auth.LoginActivity
import com.example.skripsi.viewmodel.ClassificationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraObserver: CameraLifecycleObserver
    private lateinit var cameraExecutor: ExecutorService
    private val viewModel: ClassificationViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraObserver.startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        cameraExecutor = Executors.newSingleThreadExecutor()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        splashScreen.setKeepOnScreenCondition { true }
        Handler(Looper.getMainLooper()).postDelayed({
            splashScreen.setKeepOnScreenCondition { false }
        }, 1500)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUI()
        checkAndRequestPermissions()

        cameraObserver = CameraLifecycleObserver(
            context = this,
            previewView = binding.cameraTextureView,
            cameraExecutor = cameraExecutor,
            lifecycleOwner = this
        )
        lifecycle.addObserver(cameraObserver)

        viewModel.classificationResult.observe(this) { result ->
            result?.let {
                showResultFragment(it.disease, it.confidence, it.recommendations, selectedImageUri)
            }
        }
    }

    private fun setupUI() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.buttonCamera.setOnClickListener { capturePhoto() }
        binding.buttonGallery.setOnClickListener { openGallery() }
        binding.buttonHistory.setOnClickListener { openHistory() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun capturePhoto() {
        cameraObserver.capturePhoto(
            onImageSaved = { file ->
                lifecycleScope.launch(Dispatchers.Main) {
                    selectedImageUri = Uri.fromFile(file)
                    viewModel.classifyImage(userId, file)
                }
            },
            onError = { exception ->
                Toast.makeText(this, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onPause() {
        super.onPause()
        cameraObserver.stopCamera()
    }

    override fun onStop() {
        super.onStop()
        cameraObserver.stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission()) {
            cameraObserver.startCamera()
        }
    }

    private fun openGallery() {
        cameraObserver.stopCamera()
        galleryLauncher.launch("image/*")
    }

    private fun openHistory() {
        cameraObserver.stopCamera()
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    private fun checkAndRequestPermissions() {
        if (!hasCameraPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val file = uriToFile(uri)
            viewModel.classifyImage(userId, file)
        } else {
            cameraObserver.startCamera()
        }
    }


    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalStateException("Failed to open input stream")
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return file
    }

    private fun showResultFragment(result: String, confidence: String, recommendations: String?, imageUri: Uri?) {
        val resultFragment = ResultFragment.newInstance(result, confidence, recommendations, imageUri ?: Uri.EMPTY)
        resultFragment.show(supportFragmentManager, "ResultFragment")
    }
}
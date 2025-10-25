package com.lessup.medledger.ui.scan

import android.Manifest
import android.content.Context
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    visitId: Long? = null,
    onClose: () -> Unit,
    vm: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(checkCameraPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasPermission) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("需要相机权限以进行扫描")
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("授权相机") }
            Button(onClick = onClose) { Text("返回") }
        }
        return
    }

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    val imageCapture = remember { ImageCapture.Builder().setTargetRotation(android.view.Surface.ROTATION_0).build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    var lastSaved by remember { mutableStateOf<String?>(null) }
    var pdfSaved by remember { mutableStateOf<String?>(null) }
    val captured by vm.captured.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = {
                val pv = PreviewView(it)
                previewView = pv
                pv
            },
            update = { pv ->
                bindCameraUseCases(context, lifecycleOwner, pv, imageCapture, cameraExecutor)
            }
        )
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (lastSaved != null) Text("已保存图片: ${lastSaved}")
            Text("已拍摄: ${captured.size} 张")
            if (pdfSaved != null) Text("已导出PDF: ${pdfSaved}")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    captureAndSave(context, imageCapture, cameraExecutor) { path ->
                        lastSaved = path
                        vm.saveCaptured(path, visitId)
                    }
                }) { Text("拍照保存") }
                Button(onClick = { vm.clearCaptured(); lastSaved = null; pdfSaved = null }, enabled = captured.isNotEmpty()) { Text("清空已拍") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    vm.exportPdf(context, visitId) { path ->
                        pdfSaved = path
                        vm.clearCaptured()
                    }
                }, enabled = captured.size >= 2) { Text("导出为 PDF") }
                Button(onClick = onClose) { Text("完成") }
            }
        }
    }
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (_: Exception) {
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun captureAndSave(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onSaved: (String) -> Unit
) {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    val file = File(dir, name)
    val output = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(output, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
        }
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            onSaved(file.absolutePath)
        }
    })
}

private fun checkCameraPermission(context: Context): Boolean {
    val pm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    return pm == android.content.pm.PackageManager.PERMISSION_GRANTED
}

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    visitId: Long? = null,
    onClose: () -> Unit,
    vm: ScanViewModel = koinViewModel()
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("扫描文档") },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "需要相机权限",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "请授予相机权限以扫描文档",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("授予相机权限")
                }
            }
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
    var isCapturing by remember { mutableStateOf(false) }
    val captured by vm.captured.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // 相机预览
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val pv = PreviewView(it)
                previewView = pv
                pv
            },
            update = { pv ->
                bindCameraUseCases(context, lifecycleOwner, pv, imageCapture, cameraExecutor)
            }
        )

        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onClose,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "关闭")
            }

            if (captured.isNotEmpty()) {
                AssistChip(
                    onClick = {},
                    label = { Text("${captured.size} 张") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            }
        }

        // 底部控制面板
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 已拍摄的图片预览
            if (captured.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(captured) { path ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            // 使用简单的占位符代替 Coil，因为可能没有添加依赖
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 成功提示
            pdfSaved?.let { path ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "PDF 已保存",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 拍照按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 清空按钮
                FilledTonalIconButton(
                    onClick = { vm.clearCaptured(); lastSaved = null; pdfSaved = null },
                    enabled = captured.isNotEmpty()
                ) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = "清空")
                }

                // 拍照按钮
                FilledIconButton(
                    onClick = {
                        isCapturing = true
                        captureAndSave(context, imageCapture, cameraExecutor) { path ->
                            lastSaved = path
                            vm.saveCaptured(path, visitId)
                            isCapturing = false
                        }
                    },
                    enabled = !isCapturing,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "拍照",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // 导出 PDF 按钮
                FilledTonalIconButton(
                    onClick = {
                        vm.exportPdf(context, visitId) { path ->
                            pdfSaved = path
                            vm.clearCaptured()
                        }
                    },
                    enabled = captured.size >= 2
                ) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = "导出PDF")
                }
            }

            Spacer(Modifier.height(8.dp))

            // 完成按钮
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("完成")
            }

            Spacer(Modifier.navigationBarsPadding())
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

package com.lessup.medledger.ui.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lessup.medledger.notifications.ReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var lastExportPath by remember { mutableStateOf<String?>(null) }

    val permissionGranted: MutableState<Boolean> = remember { mutableStateOf(isNotificationGranted(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted.value = granted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 通知设置
        SettingsSection(title = "通知设置") {
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "通知权限",
                subtitle = if (permissionGranted.value) "已授权" else "未授权",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted.value) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                trailing = {
                    Switch(
                        checked = permissionGranted.value,
                        onCheckedChange = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted.value) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
            )
            SettingsItem(
                icon = Icons.Outlined.NotificationsActive,
                title = "测试通知",
                subtitle = "发送一条测试提醒通知",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted.value) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        enqueueTestReminder(context)
                        Toast.makeText(context, "测试通知已发送", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // 数据管理
        SettingsSection(title = "数据管理") {
            SettingsItem(
                icon = Icons.Outlined.CloudUpload,
                title = "备份数据",
                subtitle = "导出所有数据为 ZIP 压缩包",
                onClick = {
                    scope.launch {
                        isExporting = true
                        try {
                            val path = exportDataToZip(context)
                            lastExportPath = path
                            Toast.makeText(context, "备份成功: $path", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "备份失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isExporting = false
                        }
                    }
                },
                trailing = {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                    }
                }
            )
            
            lastExportPath?.let { path ->
                SettingsItem(
                    icon = Icons.Outlined.Share,
                    title = "分享备份文件",
                    subtitle = File(path).name,
                    onClick = {
                        try {
                            val file = File(path)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/zip"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "分享备份文件"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            SettingsItem(
                icon = Icons.Outlined.DeleteForever,
                title = "清除所有数据",
                subtitle = "删除所有就诊记录和慢病档案",
                onClick = { showClearDialog = true },
                titleColor = MaterialTheme.colorScheme.error
            )
        }

        // 关于
        SettingsSection(title = "关于") {
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "关于病历本",
                subtitle = "版本 0.1.0",
                onClick = { showAboutDialog = true }
            )
            SettingsItem(
                icon = Icons.Outlined.Security,
                title = "隐私说明",
                subtitle = "所有数据均存储在本地设备",
                onClick = { }
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    // 关于对话框
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Outlined.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("病历本 Med Ledger") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("版本：0.1.0")
                    Text("一款优雅、实用的个人病历管理应用。")
                    Spacer(Modifier.height(8.dp))
                    Text("功能特点：", style = MaterialTheme.typography.titleSmall)
                    Text("• 就诊记录管理")
                    Text("• 处方/文档扫描")
                    Text("• 慢病复查提醒")
                    Text("• 搜索与筛选")
                    Text("• 本地数据备份")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "所有数据均存储在本地设备，保护您的隐私安全。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("确定") }
            }
        )
    }

    // 清除数据确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("确认清除所有数据") },
            text = { Text("此操作将删除所有就诊记录、慢病档案和扫描文档。建议先备份数据。此操作不可撤销！") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: 实现清除数据功能
                        Toast.makeText(context, "功能开发中...", Toast.LENGTH_SHORT).show()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == MaterialTheme.colorScheme.error) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing?.invoke() ?: Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun enqueueTestReminder(context: android.content.Context) {
    val request = OneTimeWorkRequestBuilder<ReminderWorker>().build()
    WorkManager.getInstance(context).enqueue(request)
}

private fun isNotificationGranted(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else true
}

private suspend fun exportDataToZip(context: android.content.Context): String = withContext(Dispatchers.IO) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "medledger_backup_$timestamp.zip"
    val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
    val outputFile = File(outputDir, fileName)
    
    ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
        // 导出数据库
        val dbFile = context.getDatabasePath("medledger.db")
        if (dbFile.exists()) {
            zipOut.putNextEntry(ZipEntry("medledger.db"))
            FileInputStream(dbFile).use { it.copyTo(zipOut) }
            zipOut.closeEntry()
        }
        
        // 导出图片目录
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        picturesDir?.listFiles()?.forEach { file ->
            if (file.isFile) {
                zipOut.putNextEntry(ZipEntry("pictures/${file.name}"))
                FileInputStream(file).use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }
        
        // 导出文档目录
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        documentsDir?.listFiles()?.filter { it.extension == "pdf" }?.forEach { file ->
            zipOut.putNextEntry(ZipEntry("documents/${file.name}"))
            FileInputStream(file).use { it.copyTo(zipOut) }
            zipOut.closeEntry()
        }
    }
    
    outputFile.absolutePath
}

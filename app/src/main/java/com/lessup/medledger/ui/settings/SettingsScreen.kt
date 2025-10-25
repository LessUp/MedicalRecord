package com.lessup.medledger.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lessup.medledger.notifications.ReminderWorker

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    val permissionGranted: MutableState<Boolean> = remember { mutableStateOf(isNotificationGranted(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted.value = granted
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted.value) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                enqueueTestReminder(context)
            }
        }) { Text("测试提醒通知") }

        Text("若设备为 Android 13+，需要授予通知权限；点击上方按钮可触发一次测试提醒。")
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

package com.lessup.medledger.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lessup.medledger.model.User
import com.lessup.medledger.sync.SyncState
import com.lessup.medledger.ui.auth.AuthState
import com.lessup.medledger.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogin: () -> Unit,
    onFamilyMembers: () -> Unit,
    onSettings: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val syncState by authViewModel.syncState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 用户信息卡片
            item {
                UserInfoCard(
                    authState = authState,
                    onLogin = onLogin,
                    onLogout = { showLogoutDialog = true }
                )
            }
            
            // 同步状态卡片
            if (authState is AuthState.LoggedIn) {
                item {
                    SyncStatusCard(
                        syncState = syncState,
                        onSyncNow = {
                            scope.launch { authViewModel.syncNow() }
                        }
                    )
                }
            }
            
            // 功能菜单
            item {
                MenuCard(
                    title = "数据管理",
                    items = listOf(
                        MenuItem(
                            icon = Icons.Outlined.People,
                            title = "家庭成员",
                            subtitle = "管理家人的健康档案",
                            onClick = onFamilyMembers
                        ),
                        MenuItem(
                            icon = Icons.Outlined.Backup,
                            title = "数据备份",
                            subtitle = "导出或恢复您的数据",
                            onClick = { /* TODO */ }
                        ),
                        MenuItem(
                            icon = Icons.Outlined.CloudSync,
                            title = "云同步设置",
                            subtitle = if (authState is AuthState.LoggedIn) "已开启" else "登录后可用",
                            enabled = authState is AuthState.LoggedIn,
                            onClick = { /* TODO */ }
                        )
                    )
                )
            }
            
            item {
                MenuCard(
                    title = "更多",
                    items = listOf(
                        MenuItem(
                            icon = Icons.Outlined.Settings,
                            title = "设置",
                            onClick = onSettings
                        ),
                        MenuItem(
                            icon = Icons.Outlined.Help,
                            title = "帮助与反馈",
                            onClick = { /* TODO */ }
                        ),
                        MenuItem(
                            icon = Icons.Outlined.Info,
                            title = "关于",
                            onClick = { /* TODO */ }
                        )
                    )
                )
            }
        }
    }
    
    // 登出确认对话框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
            title = { Text("确认退出登录？") },
            text = { Text("退出后，云同步将暂停，本地数据不会丢失。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.logout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("退出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun UserInfoCard(
    authState: AuthState,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        when (authState) {
            is AuthState.LoggedIn -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 头像
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = authState.user.nickname,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        authState.user.phone?.let { phone ->
                            Text(
                                text = phone.replaceRange(3, 7, "****"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "退出登录",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "登录后同步数据到云端",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onLogin,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("立即登录")
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    syncState: SyncState,
    onSyncNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudDone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (syncState) {
                        SyncState.Idle -> "等待同步"
                        SyncState.Syncing -> "正在同步"
                        is SyncState.Success -> "数据已同步"
                        is SyncState.Error -> "同步失败"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = when (syncState) {
                        is SyncState.Success -> "最后同步: " + java.text.SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(syncState.timestamp))
                        is SyncState.Error -> syncState.message,
                        SyncState.Syncing -> "正在与云端对齐...",
                        SyncState.Idle -> "等待首次同步"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (syncState is SyncState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSyncNow, enabled = syncState != SyncState.Syncing) {
                if (syncState == SyncState.Syncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("立即同步")
                }
            }
        }
    }
}

data class MenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

@Composable
private fun MenuCard(
    title: String,
    items: List<MenuItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            
            items.forEachIndexed { index, item ->
                Surface(
                    onClick = item.onClick,
                    enabled = item.enabled,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (item.enabled) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (item.enabled) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                            item.subtitle?.let { subtitle ->
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = if (item.enabled) 1f else 0.38f
                                    )
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (item.enabled) 1f else 0.38f
                            )
                        )
                    }
                }
                
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

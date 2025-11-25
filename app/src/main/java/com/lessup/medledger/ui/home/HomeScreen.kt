package com.lessup.medledger.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lessup.medledger.data.entity.Visit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEdit: (Long?) -> Unit,
    onScan: () -> Unit,
    onCalendar: () -> Unit = {},
    vm: HomeViewModel = hiltViewModel()
) {
    val visits by vm.visits.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Visit?>(null) }
    val focusManager = LocalFocusManager.current

    // 过滤后的就诊记录
    val filteredVisits = remember(visits, searchQuery, selectedFilter) {
        visits.filter { visit ->
            val matchesSearch = searchQuery.isBlank() ||
                visit.hospital.contains(searchQuery, ignoreCase = true) ||
                visit.department?.contains(searchQuery, ignoreCase = true) == true ||
                visit.doctor?.contains(searchQuery, ignoreCase = true) == true ||
                visit.items?.contains(searchQuery, ignoreCase = true) == true
            val matchesFilter = when (selectedFilter) {
                "thisMonth" -> {
                    val visitDate = Instant.ofEpochMilli(visit.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    val now = LocalDate.now()
                    visitDate.year == now.year && visitDate.month == now.month
                }
                "thisYear" -> {
                    val visitDate = Instant.ofEpochMilli(visit.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    visitDate.year == LocalDate.now().year
                }
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    // 获取唯一的医院列表用于统计
    val stats = remember(visits) {
        val now = LocalDate.now()
        val thisMonth = visits.count { v ->
            val visitDate = Instant.ofEpochMilli(v.date).atZone(ZoneId.systemDefault()).toLocalDate()
            visitDate.year == now.year && visitDate.month == now.month
        }
        val thisYear = visits.count { v ->
            val visitDate = Instant.ofEpochMilli(v.date).atZone(ZoneId.systemDefault()).toLocalDate()
            visitDate.year == now.year
        }
        val hospitals = visits.map { it.hospital }.distinct().size
        Triple(thisMonth, thisYear, hospitals)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
    ) {
        // 搜索栏
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { focusManager.clearFocus() },
            active = false,
            onActiveChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("搜索医院、科室、医生...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Outlined.Close, contentDescription = "清除")
                    }
                }
            }
        ) {}

        // 统计卡片
        if (visits.isNotEmpty()) {
            StatsRow(
                thisMonth = stats.first,
                thisYear = stats.second,
                hospitals = stats.third,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        // 筛选标签
        FilterChipsRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = if (selectedFilter == it) null else it },
            onCalendarClick = onCalendar,
            onScanClick = onScan,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.height(8.dp))

        // 就诊记录列表
        if (filteredVisits.isEmpty()) {
            EmptyState(
                searchQuery = searchQuery,
                hasAnyVisits = visits.isNotEmpty(),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredVisits, key = { it.id }) { visit ->
                    VisitCard(
                        visit = visit,
                        onClick = { onEdit(visit.id) },
                        onDelete = { showDeleteDialog = visit },
                        modifier = Modifier.animateItem(
                            fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // 删除确认对话框
    showDeleteDialog?.let { visit ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${visit.hospital}」的就诊记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.delete(visit)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun StatsRow(
    thisMonth: Int,
    thisYear: Int,
    hospitals: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Outlined.CalendarMonth,
            value = thisMonth.toString(),
            label = "本月就诊",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Outlined.MedicalServices,
            value = thisYear.toString(),
            label = "本年就诊",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Outlined.LocalHospital,
            value = hospitals.toString(),
            label = "就诊医院",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: String?,
    onFilterSelected: (String) -> Unit,
    onCalendarClick: () -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == "thisMonth",
                onClick = { onFilterSelected("thisMonth") },
                label = { Text("本月") },
                leadingIcon = if (selectedFilter == "thisMonth") {
                    { Icon(Icons.Outlined.FilterList, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == "thisYear",
                onClick = { onFilterSelected("thisYear") },
                label = { Text("今年") },
                leadingIcon = if (selectedFilter == "thisYear") {
                    { Icon(Icons.Outlined.FilterList, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )
        }
        item {
            AssistChip(
                onClick = onCalendarClick,
                label = { Text("日历视图") },
                leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, Modifier.size(16.dp)) }
            )
        }
        item {
            AssistChip(
                onClick = onScanClick,
                label = { Text("扫描文档") },
                leadingIcon = { Icon(Icons.Outlined.DocumentScanner, contentDescription = null, Modifier.size(16.dp)) }
            )
        }
    }
}

@Composable
private fun VisitCard(
    visit: Visit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visitDate = remember(visit.date) {
        Instant.ofEpochMilli(visit.date).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val dateText = remember(visitDate) {
        visitDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
    }
    val daysDiff = remember(visitDate) {
        ChronoUnit.DAYS.between(visitDate, LocalDate.now()).toInt()
    }
    val relativeTime = remember(daysDiff) {
        when {
            daysDiff == 0 -> "今天"
            daysDiff == 1 -> "昨天"
            daysDiff < 7 -> "${daysDiff}天前"
            daysDiff < 30 -> "${daysDiff / 7}周前"
            daysDiff < 365 -> "${daysDiff / 30}个月前"
            else -> "${daysDiff / 365}年前"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 日期指示器
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = visitDate.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${visitDate.monthValue}月",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 主要内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = visit.hospital,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visit.department?.let { dept ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(dept, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    visit.doctor?.let { doc ->
                        Text(
                            text = doc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    visit.cost?.let { cost ->
                        Text(
                            text = "¥%.2f".format(cost),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                visit.items?.takeIf { it.isNotBlank() }?.let { items ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = items,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    searchQuery: String,
    hasAnyVisits: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (searchQuery.isNotEmpty()) Icons.Outlined.Search else Icons.Outlined.Description,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) {
                "未找到匹配「$searchQuery」的记录"
            } else {
                "还没有就诊记录"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) {
                "试试其他关键词，或清空搜索查看全部"
            } else {
                "点击右下角按钮添加您的第一条就诊记录"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

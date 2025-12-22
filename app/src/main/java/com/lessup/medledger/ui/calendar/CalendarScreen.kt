package com.lessup.medledger.ui.calendar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lessup.medledger.model.Visit
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onClose: () -> Unit,
    onVisitClick: (Long) -> Unit,
    vm: CalendarViewModel = koinViewModel()
) {
    val visits by vm.visits.collectAsStateWithLifecycle()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // 按日期分组的就诊记录
    val visitsByDate = remember(visits) {
        visits.groupBy { visit ->
            Instant.ofEpochMilli(visit.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    // 选中日期的就诊记录
    val selectedVisits = selectedDate?.let { visitsByDate[it] } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历视图") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 月份选择器
            MonthSelector(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 星期标题
            WeekDayHeader(modifier = Modifier.padding(horizontal = 8.dp))

            // 日历网格
            CalendarGrid(
                yearMonth = currentMonth,
                visitsByDate = visitsByDate,
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = if (selectedDate == date) null else date
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // 选中日期的就诊记录
            if (selectedDate != null) {
                Text(
                    text = selectedDate!!.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (selectedVisits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "当天没有就诊记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedVisits, key = { it.localId }) { visit ->
                            VisitItem(
                                visit = visit,
                                onClick = { onVisitClick(visit.localId) }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            } else {
                // 显示本月统计
                val monthVisits = visitsByDate.entries
                    .filter { it.key.year == currentMonth.year && it.key.monthValue == currentMonth.monthValue }
                    .flatMap { it.value }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${monthVisits.size}",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "本月就诊次数",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "点击日期查看详情",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = "上个月")
        }
        Text(
            text = "${currentMonth.year}年${currentMonth.monthValue}月",
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "下个月")
        }
    }
}

@Composable
private fun WeekDayHeader(modifier: Modifier = Modifier) {
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
    Row(modifier = modifier.fillMaxWidth()) {
        weekDays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    visitsByDate: Map<LocalDate, List<Visit>>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    // 生成日历格子数据
    val calendarDays = buildList {
        // 前面的空白
        repeat(firstDayOfWeek) { add(null) }
        // 本月的天数
        for (day in 1..daysInMonth) {
            add(yearMonth.atDay(day))
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.height(((calendarDays.size / 7 + 1) * 48).dp),
        userScrollEnabled = false
    ) {
        items(calendarDays) { date ->
            if (date == null) {
                Box(modifier = Modifier.size(48.dp))
            } else {
                val hasVisit = visitsByDate.containsKey(date)
                val visitCount = visitsByDate[date]?.size ?: 0
                val isSelected = date == selectedDate
                val isToday = date == today

                DayCell(
                    day = date.dayOfMonth,
                    hasVisit = hasVisit,
                    visitCount = visitCount,
                    isSelected = isSelected,
                    isToday = isToday,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    hasVisit: Boolean,
    visitCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (hasVisit) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.tertiary
                        )
                )
            }
        }
    }
}

@Composable
private fun VisitItem(
    visit: Visit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = visit.hospital,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visit.department?.let { dept ->
                        Text(
                            text = dept,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
            visit.cost?.let { cost ->
                Text(
                    text = "¥%.0f".format(cost),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

package com.lessup.medledger.ui.chronic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lessup.medledger.data.repository.ChronicRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronicScreen(
    vm: ChronicViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val isSaving by vm.isSaving.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "新增慢病")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.conditions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("还没有慢病档案")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "点击右下角按钮新建慢病，并设置复查计划。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.conditions, key = { it.condition.id }) { overview ->
                        ConditionCard(overview = overview, formatter = formatter)
                    }
                    item { Spacer(modifier = Modifier.padding(bottom = 32.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        AddChronicDialog(
            isSaving = isSaving,
            onDismiss = { showDialog = false },
            onSave = {
                vm.createCondition(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ConditionCard(
    overview: ChronicRepository.ConditionOverview,
    formatter: DateTimeFormatter
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = overview.condition.name,
                style = MaterialTheme.typography.titleMedium
            )
            val note = overview.condition.note?.takeIf { it.isNotBlank() }
            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (overview.plans.isEmpty()) {
                Text("尚未设置复查计划", style = MaterialTheme.typography.bodySmall)
            } else {
                overview.plans.forEach { plan ->
                    PlanItem(plan = plan, formatter = formatter)
                }
            }
        }
    }
}

@Composable
private fun PlanItem(
    plan: ChronicRepository.PlanOverview,
    formatter: DateTimeFormatter
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val items = plan.plan.items?.takeIf { it.isNotBlank() }
        Text(
            text = items ?: "复查项目未填写",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        val nextCheck = plan.nextCheckDate?.format(formatter) ?: "未定"
        Text("下一次复查：$nextCheck", style = MaterialTheme.typography.bodySmall)
        val remind = plan.remindDate?.format(formatter)
        Text(
            text = if (remind != null) "提醒日期：$remind" else "将在复查当日提醒",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChronicDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (AddChronicInput) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var planItems by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val intervals = listOf(1, 3, 6, 12)
    var selectedInterval by remember { mutableStateOf(3) }
    var remindDays by remember { mutableStateOf("3") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    if (showDatePicker) {
        val initial = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = state.selectedDateMillis
                    if (selected != null) {
                        startDate = Instant.ofEpochMilli(selected).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    val canSave = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val remind = remindDays.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                onSave(
                    AddChronicInput(
                        name = name,
                        planItems = planItems.ifBlank { null },
                        intervalMonths = selectedInterval,
                        startDate = startDate,
                        remindDaysBefore = remind,
                        diagnosedAt = startDate,
                        note = note.ifBlank { null }
                    )
                )
            }, enabled = canSave && !isSaving) {
                Text(if (isSaving) "保存中..." else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("取消") }
        },
        title = { Text("新增慢病档案") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("慢病名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = planItems,
                    onValueChange = { planItems = it },
                    label = { Text("复查项目（可选）") },
                    minLines = 1,
                    maxLines = 3
                )
                Text("复查周期", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    intervals.forEach { interval ->
                        FilterChip(
                            selected = interval == selectedInterval,
                            onClick = { selectedInterval = interval },
                            label = { Text("${interval}个月") }
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("复查起始日期", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(startDate.format(formatter))
                        TextButton(onClick = { showDatePicker = true }) { Text("选择") }
                    }
                }
                OutlinedTextField(
                    value = remindDays,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) remindDays = value
                    },
                    label = { Text("提前提醒天数（可选）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    minLines = 1,
                    maxLines = 3
                )
            }
        }
    )
}

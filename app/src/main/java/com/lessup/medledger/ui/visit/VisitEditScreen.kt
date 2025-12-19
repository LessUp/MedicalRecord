package com.lessup.medledger.ui.visit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitEditScreen(
    visitId: Long?,
    onClose: () -> Unit,
    vm: VisitEditViewModel = koinViewModel()
) {
    LaunchedEffect(visitId) { vm.load(visitId) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    // 日期选择器
    if (showDatePicker) {
        val currentDate = runCatching { LocalDate.parse(ui.dateText) }.getOrElse { LocalDate.now() }
        val initial = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(formatter)
                        vm.update { it.copy(dateText = selected) }
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ui.id == null) "新增就诊" else "编辑就诊") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { vm.save(onSaved = onClose) },
                        enabled = !ui.saving,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (ui.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Save, contentDescription = null, Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(if (ui.id == null) "保存" else "更新")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 错误提示
            AnimatedVisibility(visible = ui.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = ui.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 基本信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "基本信息",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 日期选择
                    OutlinedTextField(
                        value = ui.dateText,
                        onValueChange = { text -> vm.update { state -> state.copy(dateText = itLimit(text, 10)) } },
                        label = { Text("就诊日期") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Text("选择", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    // 医院
                    OutlinedTextField(
                        value = ui.hospital,
                        onValueChange = { text -> vm.update { state -> state.copy(hospital = itLimit(text, 50)) } },
                        label = { Text("医院 *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Outlined.LocalHospital, contentDescription = null) },
                        singleLine = true,
                        isError = ui.error?.contains("医院") == true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    // 科室和医生并排
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ui.department,
                            onValueChange = { text -> vm.update { state -> state.copy(department = itLimit(text, 50)) } },
                            label = { Text("科室") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Outlined.MedicalServices, contentDescription = null, Modifier.size(20.dp)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = ui.doctor,
                            onValueChange = { text -> vm.update { state -> state.copy(doctor = itLimit(text, 50)) } },
                            label = { Text("医生") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, Modifier.size(20.dp)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }
                }
            }

            // 详细信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "详细信息",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 就诊项目
                    OutlinedTextField(
                        value = ui.items,
                        onValueChange = { text -> vm.update { state -> state.copy(items = text) } },
                        label = { Text("就诊项目") },
                        placeholder = { Text("如：体检、验血、CT 等，用逗号分隔") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    // 费用
                    OutlinedTextField(
                        value = ui.costText,
                        onValueChange = { text -> 
                            // 只允许数字和小数点
                            val filtered = text.filter { it.isDigit() || it == '.' }
                            vm.update { state -> state.copy(costText = filtered) } 
                        },
                        label = { Text("费用") },
                        placeholder = { Text("0.00") },
                        leadingIcon = { Icon(Icons.Outlined.AttachMoney, contentDescription = null) },
                        prefix = { Text("¥") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 备注
                    OutlinedTextField(
                        value = ui.note,
                        onValueChange = { text -> vm.update { state -> state.copy(note = text) } },
                        label = { Text("备注") },
                        placeholder = { Text("添加备注信息...") },
                        leadingIcon = { Icon(Icons.Outlined.Note, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
            }

            // 底部保存按钮（移动端友好）
            Button(
                onClick = { vm.save(onSaved = onClose) },
                enabled = !ui.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (ui.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Outlined.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (ui.id == null) "保存就诊记录" else "更新就诊记录")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun itLimit(it: String, max: Int): String = if (it.length <= max) it else it.take(max)

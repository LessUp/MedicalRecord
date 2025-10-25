package com.lessup.medledger.ui.visit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VisitEditScreen(
    visitId: Long?,
    onClose: () -> Unit,
    vm: VisitEditViewModel = hiltViewModel()
) {
    LaunchedEffect(visitId) { vm.load(visitId) }
    val ui by vm.ui.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (ui.error != null) {
            Text(text = ui.error!!)
        }
        OutlinedTextField(
            value = ui.dateText,
            onValueChange = { text -> vm.update { state -> state.copy(dateText = itLimit(text, 10)) } },
            label = { Text("日期（YYYY-MM-DD）") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.hospital,
            onValueChange = { text -> vm.update { state -> state.copy(hospital = itLimit(text, 50)) } },
            label = { Text("医院") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.department,
            onValueChange = { text -> vm.update { state -> state.copy(department = itLimit(text, 50)) } },
            label = { Text("科室") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.doctor,
            onValueChange = { text -> vm.update { state -> state.copy(doctor = itLimit(text, 50)) } },
            label = { Text("医生") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.items,
            onValueChange = { text -> vm.update { state -> state.copy(items = text) } },
            label = { Text("就诊项目（逗号分隔）") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.costText,
            onValueChange = { text -> vm.update { state -> state.copy(costText = text) } },
            label = { Text("费用") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.note,
            onValueChange = { text -> vm.update { state -> state.copy(note = text) } },
            label = { Text("备注") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.save(onSaved = onClose) }, enabled = !ui.saving, modifier = Modifier.fillMaxWidth()) {
            Text(if (ui.id == null) "保存" else "更新")
        }
    }
}

private fun itLimit(it: String, max: Int): String = if (it.length <= max) it else it.take(max)

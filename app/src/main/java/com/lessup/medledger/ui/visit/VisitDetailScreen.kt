package com.lessup.medledger.ui.visit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun VisitDetailScreen(
    visitId: Long,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onScan: () -> Unit,
    vm: VisitDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(visitId) { vm.load(visitId) }

    val visit by vm.visit.collectAsStateWithLifecycle()
    val docs = vm.docs?.collectAsStateWithLifecycle()?.value.orEmpty()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onClose) { Text("返回") }
            Button(onClick = onEdit) { Text("编辑") }
            Button(onClick = onScan) { Text("去扫描并关联") }
        }

        if (visit == null) {
            Text("加载中…")
            return
        }

        val v = visit!!
        val date = Instant.ofEpochMilli(v.date).atZone(ZoneId.systemDefault())
            .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)

        ListItem(
            headlineContent = { Text(v.hospital, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            supportingContent = {
                Text(listOfNotNull(date, v.department, v.doctor).joinToString(" · "))
            }
        )

        Divider()
        Text("关联文档（${docs.size}）")
        if (docs.isEmpty()) {
            Text("暂无文档，点击上方“去扫描并关联”以添加")
        } else {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(docs, key = { it.id }) { d ->
                    ListItem(
                        headlineContent = { Text(d.title) },
                        supportingContent = { Text(d.path, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    Divider()
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

package com.lessup.medledger.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lessup.medledger.data.entity.Visit
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onEdit: (Long?) -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val visits by vm.visits.collectAsStateWithLifecycle()

    if (visits.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无就诊记录，点击右下角 + 新增")
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize()) {
        items(visits, key = { it.id }) { v ->
            VisitRow(
                visit = v,
                onClick = { onEdit(v.id) },
                onDelete = { vm.delete(v) }
            )
            Divider()
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun VisitRow(
    visit: Visit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val date = Instant.ofEpochMilli(visit.date).atZone(ZoneId.systemDefault())
        .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)

    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(visit.hospital, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(
                listOfNotNull(
                    date,
                    visit.department,
                    visit.doctor
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "删除")
            }
        }
    )
}

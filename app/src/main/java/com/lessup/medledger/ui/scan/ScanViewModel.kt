package com.lessup.medledger.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.repository.DocumentRepository
import kotlinx.coroutines.launch
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import com.lessup.medledger.util.PdfUtils

class ScanViewModel(
    private val repo: DocumentRepository
) : ViewModel() {

    private val _captured = MutableStateFlow<List<String>>(emptyList())
    val captured: StateFlow<List<String>> = _captured.asStateFlow()

    fun saveCaptured(path: String, visitId: Long? = null, onDone: (() -> Unit)? = null) {
        val title = File(path).nameWithoutExtension
        viewModelScope.launch {
            repo.insertScan(localPath = path, title = title, visitId = visitId)
            onDone?.invoke()
        }
        // 收集到当前拍摄的图片，便于多页导出
        _captured.value = _captured.value + path
    }

    fun clearCaptured() {
        _captured.value = emptyList()
    }

    fun exportPdf(context: Context, visitId: Long? = null, onDone: ((String) -> Unit)? = null) {
        val images = _captured.value
        if (images.isEmpty()) return
        viewModelScope.launch {
            val pdfPath = PdfUtils.imagesToPdf(context, images, fileNamePrefix = "scan")
            val title = File(pdfPath).nameWithoutExtension
            repo.insertScan(localPath = pdfPath, title = title, visitId = visitId, pages = images.size)
            onDone?.invoke(pdfPath)
        }
    }
}

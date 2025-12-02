package com.lessup.medledger.ui.settings

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.data.db.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) : ViewModel() {

    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()

    data class ClearResult(
        val deletedPictures: Int,
        val deletedDocuments: Int
    )

    fun clearAllData(onFinished: (ClearResult) -> Unit, onError: (Throwable) -> Unit) {
        if (_isClearing.value) return

        viewModelScope.launch {
            _isClearing.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    database.clearAllTables()
                    val pictures = clearDirectory(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                    val documents = clearDirectory(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS))
                    ClearResult(pictures, documents)
                }
                onFinished(result)
            } catch (t: Throwable) {
                onError(t)
            } finally {
                _isClearing.value = false
            }
        }
    }

    private fun clearDirectory(directory: File?): Int {
        var deleted = 0
        directory?.listFiles()?.forEach { file ->
            if (file.isFile && file.delete()) {
                deleted++
            }
        }
        return deleted
    }
}

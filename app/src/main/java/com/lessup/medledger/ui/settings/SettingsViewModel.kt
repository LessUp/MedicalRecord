package com.lessup.medledger.ui.settings

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.db.MedLedgerDatabase
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val context: Context,
    private val database: MedLedgerDatabase
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
                    database.transaction {
                        database.checkupPlanQueries.deleteAll()
                        database.chronicConditionQueries.deleteAll()
                        database.documentQueries.deleteAll()
                        database.visitQueries.deleteAll()
                        database.familyMemberQueries.deleteAll()
                        database.userQueries.deleteAll()
                    }
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

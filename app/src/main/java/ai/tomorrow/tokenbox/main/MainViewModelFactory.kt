package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.data.HistoryDao
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModelFactory(
    private val application: Application,
    private val database: HistoryDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
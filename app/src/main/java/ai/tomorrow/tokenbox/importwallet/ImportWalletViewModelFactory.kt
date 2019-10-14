package ai.tomorrow.tokenbox.importwallet

import ai.tomorrow.tokenbox.main.WalletViewModel
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ImportWalletViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportWalletViewModel::class.java)) {
            return ImportWalletViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
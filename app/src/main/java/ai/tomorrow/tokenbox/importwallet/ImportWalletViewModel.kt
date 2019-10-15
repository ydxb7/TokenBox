package ai.tomorrow.tokenbox.importwallet

import ai.tomorrow.tokenbox.repository.WalletRepository
import ai.tomorrow.tokenbox.utils.Result
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File


class ImportWalletViewModel(
    private val application: Application
) : ViewModel(), KeystoreStorage, KoinComponent {
    override fun getKeystoreDir(): File {
        return application.filesDir
    }

    private val TAG = "ImportWalletViewModel"

    val walletRepository: WalletRepository by inject()

    init {
        // tokenCore
        WalletManager.storage = this
        WalletManager.scanWallets()
    }
    
    /**
     * generate wallet from mnemonic
     */
    fun generateWalletFromMnemonic(
        mnemonicInput: String, password: String, passwordHint: String,
        nameInput: String
    ): Boolean {
        Log.d(TAG, "generateWalletFromMnemonic")

        var isGenerate = false

        walletRepository.createAndSaveWallet(mnemonicInput, password, passwordHint, nameInput) {
            when (it) {
                is Result.Success -> {
                    Toast.makeText(application, "Wallet has been created", Toast.LENGTH_SHORT)
                        .show()
                    isGenerate = true
                }
                is Result.Failure -> Toast.makeText(
                    application,
                    "Generate wallet fail",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return isGenerate
    }
}
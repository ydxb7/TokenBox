package ai.tomorrow.tokenbox.repository

import ai.tomorrow.tokenbox.data.ResultResponse
import ai.tomorrow.tokenbox.data.Wallet
import ai.tomorrow.tokenbox.datasource.WalletDatasource
import ai.tomorrow.tokenbox.utils.Result
import android.app.Application
import android.widget.Toast

class WalletRepository(val application: Application) {

    val walletDatasource = WalletDatasource(application)

    fun getWalletFromPreference(): Wallet = walletDatasource.getWalletFromPreference()
    fun saveWalletInPreference(wallet: Wallet) = walletDatasource.saveWalletInPreference(wallet)

    fun createAndSaveWallet(
        mnemonic: String, password: String, passwordHint: String,
        name: String,
        callback: (Result<String, Exception>) -> Unit
    ) {
        val wallet = walletDatasource.createWalletFromMnemonic(mnemonic, password, passwordHint, name, callback)

        if (wallet != null){
            walletDatasource.saveWalletInPreference(wallet)
        }
    }
}
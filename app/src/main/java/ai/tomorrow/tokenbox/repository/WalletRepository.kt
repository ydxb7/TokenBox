package ai.tomorrow.tokenbox.repository

import ai.tomorrow.tokenbox.data.Wallet
import ai.tomorrow.tokenbox.datasource.WalletDatasource
import android.app.Application

class WalletRepository (val application: Application){

    val walletDatasource = WalletDatasource(application)

    fun getWalletFromPreference(): Wallet = walletDatasource.getWalletFromPreference()
    fun saveWalletInPreference(wallet: Wallet) = walletDatasource.saveWalletInPreference(wallet)
}
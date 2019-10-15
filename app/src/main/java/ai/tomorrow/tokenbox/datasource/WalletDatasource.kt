package ai.tomorrow.tokenbox.datasource

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.data.DatabaseBalance
import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.Wallet
import ai.tomorrow.tokenbox.data.getDatabase
import android.app.Application
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager

class WalletDatasource(val application: Application){

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    /**
     * save Wallet info into preference
     */
    fun saveWalletInPreference(wallet: Wallet) {

        sharedPreferences.edit {
            putString(application.getString(R.string.wallet_address), wallet.address)
            putString(application.getString(R.string.wallet_name), wallet.name)
            putString(application.getString(R.string.wallet_password), wallet.password)
            putString(application.getString(R.string.wallet_password_hint), wallet.passwordHint)
            putString(application.getString(R.string.wallet_keystore), wallet.keystore)
            putString(application.getString(R.string.wallet_keystore_path), wallet.keystorePath)
            putString(application.getString(R.string.wallet_mnemonic), wallet.mnemonic)
            putString(application.getString(R.string.wallet_private_key), wallet.privateKey)
        }
    }

    fun getWalletFromPreference(): Wallet{
        return Wallet(
            sharedPreferences.getString(application.getString(R.string.wallet_address), "")?:"",
            sharedPreferences.getString(application.getString(R.string.wallet_name), "")?:"",
            sharedPreferences.getString(application.getString(R.string.wallet_password), "")?:"",
            sharedPreferences.getString(application.getString(R.string.wallet_password_hint), "")?:"",
            sharedPreferences.getString(application.getString(R.string.wallet_keystore), "")?:"",
            sharedPreferences.getString(application.getString(R.string.wallet_keystore_path), "")?:"",
            sharedPreferences.getString(application.getString(R.string.wallet_mnemonic), "")?:"",
            sharedPreferences.getString(application.getString(R.string.wallet_private_key), "")?:""
        )
    }
}
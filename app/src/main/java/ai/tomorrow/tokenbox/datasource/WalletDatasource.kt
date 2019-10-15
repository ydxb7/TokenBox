package ai.tomorrow.tokenbox.datasource

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.data.DatabaseBalance
import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.Wallet
import ai.tomorrow.tokenbox.data.getDatabase
import ai.tomorrow.tokenbox.utils.Result
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import org.consenlabs.tokencore.wallet.model.TokenException

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

    fun createWalletFromMnemonic(
        mnemonicInput: String, password: String, passwordHint: String,
        nameInput: String,
        callback: (Result<String, Exception>) -> Unit
    ): Wallet? {

        // generate wallet using tokenCore
        var identity: Identity
        try {
            identity = Identity.recoverIdentity(
                mnemonicInput,
                "identity1",
                password,
                passwordHint,
                Network.ROPSTEN,
                Metadata.NONE
            )
        } catch (e: TokenException) {
            callback.invoke(Result.Failure(e))
//            Toast.makeText(application, "mnemonic word invalid", Toast.LENGTH_SHORT).show()
            return null
        }

        val tokenCoreWallet = identity.wallets[0]

        // get all info we need
        val address = "0x${tokenCoreWallet.address}"
        val keystore = WalletManager.exportKeystore(tokenCoreWallet.id, password)
        val keystorePath =
            application.filesDir.absolutePath + "/wallets" + "/${tokenCoreWallet.id}.json"
        val privateKey = WalletManager.exportPrivateKey(tokenCoreWallet.id, password)
        val mnemonic = WalletManager.exportMnemonic(tokenCoreWallet.id, password).mnemonic
        val name = nameInput

        // save wallet info into preference
        val wallet = Wallet(
            address,
            name,
            password,
            passwordHint,
            keystore,
            keystorePath,
            mnemonic,
            privateKey
        )

        callback.invoke(Result.Success("Generate wallet success."))
        return wallet
    }

}
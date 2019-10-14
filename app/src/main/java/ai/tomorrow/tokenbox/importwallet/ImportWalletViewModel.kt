package ai.tomorrow.tokenbox.importwallet

import ai.tomorrow.tokenbox.R
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import org.consenlabs.tokencore.wallet.model.TokenException
import java.io.File


class ImportWalletViewModel(
    private val application: Application
) : ViewModel(), KeystoreStorage {
    override fun getKeystoreDir(): File {
        return application.filesDir
    }

    private val TAG = "ImportWalletViewModel"

    init {
        // tokenCore
        WalletManager.storage = this
        WalletManager.scanWallets()
    }


    /**
     * generate wallet from mnemonic
     */
    fun loadWalletFromMnemonic(mnemonicInput: String, password: String, passwordHint: String,
                                       nameInput: String): Boolean {
        Log.d(TAG, "loadWalletFromMnemonic")

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
            Toast.makeText(application, "mnemonic word invalid", Toast.LENGTH_SHORT).show()
            return false
        }

        Log.d(TAG, "get identity")

        val tokenCoreWallet = identity.wallets[0]

        // get all info we need
        val address = "0x${tokenCoreWallet.address}"
        val keystore = WalletManager.exportKeystore(tokenCoreWallet.id, password)
        val keystorePath =
            application.filesDir.absolutePath + "/wallets" + "/${tokenCoreWallet.id}.json"
        val privateKey = WalletManager.exportPrivateKey(tokenCoreWallet.id, password)
        val mnemonic = WalletManager.exportMnemonic(tokenCoreWallet.id, password).mnemonic
        val name = nameInput

        Log.d(TAG, "wallet.address = $address")
        Log.d(TAG, "wallet.keystore = $keystore")
        Log.d(TAG, "wallet.keystorePath = $keystorePath")
        Log.d(TAG, "wallet.privateKey = $privateKey")
        Log.d(TAG, "wallet.mnemonic = $mnemonic")
        Log.d(TAG, "wallet.name = $name")

        // save wallet info into preference
        saveWalletInPreference(
            address,
            name,
            password,
            passwordHint,
            keystore,
            keystorePath,
            mnemonic,
            privateKey
        )
        return true
    }


    /**
     * save Wallet info into preference
     */
    private fun saveWalletInPreference(
        address: String,
        name: String,
        password: String,
        passwordHint: String,
        keystore: String?,
        keystorePath: String,
        mnemonic: String?,
        privateKey: String?
    ) {
        Log.d(TAG, "saveWalletInPreference")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        sharedPreferences.edit {
            putString(application.getString(R.string.wallet_address), address)
            putString(application.getString(R.string.wallet_name), name)
            putString(application.getString(R.string.wallet_password), password)
            putString(application.getString(R.string.wallet_password_hint), passwordHint)
            putString(application.getString(R.string.wallet_keystore), keystore)
            putString(application.getString(R.string.wallet_keystore_path), keystorePath)
            putString(application.getString(R.string.wallet_mnemonic), mnemonic)
            putString(application.getString(R.string.wallet_private_key), privateKey)
        }
    }



}
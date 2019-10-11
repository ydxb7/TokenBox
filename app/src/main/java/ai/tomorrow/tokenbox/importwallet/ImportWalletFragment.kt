package ai.tomorrow.tokenjar

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.databinding.FragmentImportWalletBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import java.io.File
import android.content.SharedPreferences
import android.R.attr.password
import androidx.core.content.edit
import androidx.preference.PreferenceManager


class ImportWalletFragment : Fragment(), KeystoreStorage {

    private val TAG = "ImportWalletFragment"

    private lateinit var binding: FragmentImportWalletBinding

    private lateinit var identity: Identity
    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentImportWalletBinding.inflate(inflater, container, false)

        WalletManager.storage = this
        WalletManager.scanWallets()

        binding.importWalletBtn.setOnClickListener {
            if (checkInputValid()){
                getWalletFromMnemonic()
                it.findNavController().navigateUp()
            }
        }



        binding.backBtn.setOnClickListener {
            it.findNavController().navigateUp()
        }

        return binding.root
    }

    private fun checkInputValid(): Boolean {
        if (binding.walletPasswordEt.text.isEmpty() || binding.repeatPasswordEt.text.isEmpty()) {
            Toast.makeText(context, "Password is required.", Toast.LENGTH_SHORT).show()
            return false
        } else if (binding.walletPasswordEt.text.toString() != binding.repeatPasswordEt.text.toString()) {
            Log.d(TAG, "password: ${binding.walletPasswordEt.text}")
            Log.d(TAG, "repeat password: ${binding.repeatPasswordEt.text}")
            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return false
        } else if (binding.mnemonicEv.text.isEmpty()) {
            Toast.makeText(context, "Mnemonic phrases is required.", Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }


    private fun getWalletFromMnemonic() {
        Log.d(TAG, "getWalletFromMnemonic")
        val mnemonicInput = binding.mnemonicEv.text.toString().trim()
        val password = binding.walletPasswordEt.text.toString().trim()
        val passwordHint = binding.passwordHintEt.text.toString().trim()
        val nameInput = binding.walletNameEt.text.toString().trim()

        identity = Identity.recoverIdentity(
            mnemonicInput,
            "identity1",
            password,
            passwordHint,
            Network.ROPSTEN,
            Metadata.NONE
        )
        Log.d(TAG, "get identity")

        val tokenCoreWallet = identity.wallets[0]


        val address = "0x${tokenCoreWallet.address}"
        val keystore = WalletManager.exportKeystore(tokenCoreWallet.id, password)
        val keystorePath =
            requireActivity().filesDir.absolutePath + "/wallets" + "/${tokenCoreWallet.id}.json"
        val privateKey = WalletManager.exportPrivateKey(tokenCoreWallet.id, password)
        val mnemonic = WalletManager.exportMnemonic(tokenCoreWallet.id, password).mnemonic
        val name = nameInput

        Log.d(TAG, "wallet.address = $address")
        Log.d(TAG, "wallet.keystore = $keystore")
        Log.d(TAG, "wallet.keystorePath = $keystorePath")
        Log.d(TAG, "wallet.privateKey = $privateKey")
        Log.d(TAG, "wallet.mnemonic = $mnemonic")
        Log.d(TAG, "wallet.name = $name")


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

    }

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit {
            putString(getString(R.string.wallet_address), address)
            putString(getString(R.string.wallet_name), name)
            putString(getString(R.string.wallet_password), password)
            putString(getString(R.string.wallet_password_hint), passwordHint)
            putString(getString(R.string.wallet_keystore), keystore)
            putString(getString(R.string.wallet_keystore_path), keystorePath)
            putString(getString(R.string.wallet_mnemonic), mnemonic)
            putString(getString(R.string.wallet_private_key), privateKey)
        }

        Log.d(TAG, "address form preference: ${sharedPreferences.getString(getString(R.string.wallet_address), "")}")
        Log.d(TAG, "name form preference: ${sharedPreferences.getString(getString(R.string.wallet_name), "")}")
        Log.d(TAG, "password form preference: ${sharedPreferences.getString(getString(R.string.wallet_password), "")}")
        Log.d(TAG, "mnemonic form preference: ${sharedPreferences.getString(getString(R.string.wallet_mnemonic), "")}")
    }

    override fun getKeystoreDir(): File {
        return requireActivity().filesDir
    }
}
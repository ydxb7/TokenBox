package ai.tomorrow.tokenjar

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.databinding.FragmentImportWalletBinding
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.zxing.integration.android.IntentIntegrator
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import org.consenlabs.tokencore.wallet.model.TokenException
import java.io.File


class ImportWalletFragment : Fragment(), KeystoreStorage {

    private val TAG = "ImportWalletFragment"

    private lateinit var binding: FragmentImportWalletBinding

    private lateinit var identity: Identity

    private var toast: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentImportWalletBinding.inflate(inflater, container, false)

        // tokenCore
        WalletManager.storage = this
        WalletManager.scanWallets()

        setupWidgets()

        return binding.root
    }

    private fun setupWidgets() {
        binding.importWalletBtn.setOnClickListener {
            if (checkInputValid()) {
                if (loadWalletFromMnemonic()) {
                    it.findNavController().navigateUp()
                }
            }
        }

        binding.scanRtn.setOnClickListener {
            scanFromFragment()
        }

        binding.backBtn.setOnClickListener {
            it.findNavController().navigateUp()
        }
    }

    /**
     * check input is valid
     */
    private fun checkInputValid(): Boolean {
        Log.d(TAG, "checkInputValid")
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
        }
        return true
    }

    /**
     * generate wallet from mnemonic
     */
    private fun loadWalletFromMnemonic(): Boolean {
        Log.d(TAG, "loadWalletFromMnemonic")
        // get input info
        val mnemonicInput = binding.mnemonicEv.text.toString().trim()
        val password = binding.walletPasswordEt.text.toString().trim()
        val passwordHint = binding.passwordHintEt.text.toString().trim()
        val nameInput = binding.walletNameEt.text.toString().trim()

        // generate wallet using tokenCore
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
            Toast.makeText(context, "mnemonic word invalid", Toast.LENGTH_SHORT).show()
            return false
        }

        Log.d(TAG, "get identity")

        val tokenCoreWallet = identity.wallets[0]

        // get all info we need
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
    }

    override fun getKeystoreDir(): File {
        return requireActivity().filesDir
    }

    //------------------------ QR code ------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated: ")

        displayToast()
    }

    fun scanFromFragment() {
        Log.d(TAG, "scanFromFragment: ")
        IntentIntegrator.forSupportFragment(this).initiateScan()
    }

    private fun displayToast() {
        Log.d(TAG, "displayToast: ")
        if (activity != null && toast != null) {
            Toast.makeText(activity, toast, Toast.LENGTH_LONG).show()
            toast = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: ")
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                toast = "Cancelled"
                displayToast()
            } else {
                binding.mnemonicEv.setText(result.contents)
            }
        }
    }
}
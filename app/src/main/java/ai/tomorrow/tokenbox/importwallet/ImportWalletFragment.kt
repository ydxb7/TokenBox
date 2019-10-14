package ai.tomorrow.tokenjar

import ai.tomorrow.tokenbox.databinding.FragmentImportWalletBinding
import ai.tomorrow.tokenbox.importwallet.ImportWalletViewModel
import ai.tomorrow.tokenbox.importwallet.ImportWalletViewModelFactory
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import org.consenlabs.tokencore.wallet.Identity


class ImportWalletFragment : Fragment() {

    private val TAG = "ImportWalletFragment"

    private lateinit var binding: FragmentImportWalletBinding

    private lateinit var identity: Identity

    private var toast: String? = null

    private lateinit var viewModel: ImportWalletViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentImportWalletBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application

        val viewModelFactory = ImportWalletViewModelFactory(application)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(ImportWalletViewModel::class.java)

        setupWidgets()

        return binding.root
    }

    private fun setupWidgets() {
        binding.importWalletBtn.setOnClickListener {
            if (checkInputValid()) {
                if (viewModel.loadWalletFromMnemonic(
                        binding.mnemonicEv.text.toString().trim(),
                        binding.walletPasswordEt.text.toString().trim(),
                        binding.passwordHintEt.text.toString().trim(),
                        binding.walletNameEt.text.toString().trim()
                    )
                ) {
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
            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return false
        } else if (binding.mnemonicEv.text.isEmpty()) {
            Toast.makeText(context, "Mnemonic phrases is required.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
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
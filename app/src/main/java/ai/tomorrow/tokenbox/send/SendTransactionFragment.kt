package ai.tomorrow.tokenbox.send

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.databinding.FragmentSendEthBinding
import ai.tomorrow.tokenbox.datasource.Web3jDatasource
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.zxing.integration.android.IntentIntegrator
import org.web3j.utils.Convert
import java.math.BigInteger

class SendTransactionFragment : Fragment() {

    private val TAG = "SendTransactionFragment"

    private lateinit var binding: FragmentSendEthBinding
    private var toast: String? = null

    private lateinit var viewModel: SendTransactionViewModel

    var gasCount = 21000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentSendEthBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = SendTransactionViewModelFactory(application)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(SendTransactionViewModel::class.java)

        // get balance from bundle
        val balanceString =
            SendTransactionFragmentArgs.fromBundle(requireNotNull(arguments)).balance
        val balance = balanceString.split(" ")[0].toBigDecimal()
        val balanceWei = Convert.toWei(balance, Convert.Unit.ETHER).toBigInteger()

        // get wallet info from preference
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val keystorePath =
            sharedPreferences.getString(getString(R.string.wallet_keystore_path), "") ?: ""
        val password = sharedPreferences.getString(getString(R.string.wallet_password), "") ?: ""
        val myAddress = sharedPreferences.getString(getString(R.string.wallet_address), "") ?: ""

        viewModel.gasPriceShow.observe(this, androidx.lifecycle.Observer {
            binding.gasPriceTV.text = it
        })

        setupWidgets(balanceString, balanceWei, password, keystorePath, myAddress)

        return binding.root
    }

    private fun setupWidgets(
        balanceString: String,
        balanceWei: BigInteger?,
        password: String,
        keystorePath: String,
        myAddress: String
    ) {
        binding.balanceTv.text = balanceString

        // scan qr
        binding.scanIv.setOnClickListener {
            scanFromFragment()
        }

        // seekbar
        binding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Log.d(TAG, "onStopTrackingTouch")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                Log.d(TAG, "onStartTrackingTouch")
            }

            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                Log.d(TAG, "onProgressChanged")
                gasCount = 11000 + progress
                binding.gasLimitTV.text = gasCount.toString()
            }
        })

        // send button
        binding.sendBtn.setOnClickListener {
            Log.d(TAG, "sendBtn clicked")

            val toAddress = binding.recipientAddressEv.text.toString()
            val gasLimitBigInteger = gasCount.toBigInteger()
            val amount = binding.amountEv.text.toString().toBigDecimal()
            val amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()

            val isValid = viewModel.checkValid(amountWei, balanceWei, gasLimitBigInteger)

            if (!isValid) {
                Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT)
                    .show()
            } else {

//                val transactionModel = Web3jDatasource.TransactionModel(
//                    password,
//                    keystorePath,
//                    myAddress,
//                    gasLimitBigInteger,
//                    toAddress,
//                    amountWei,
//                    null
//                )

                viewModel.makeTransaction(toAddress, gasLimitBigInteger, amountWei)
            }
        }

        // back button
        binding.backBtn.setOnClickListener {
            it.findNavController().navigateUp()
        }

        viewModel.navigateUp.observe(this, Observer {
            if (it == true){
                findNavController().navigateUp()
                viewModel.navagationUpDone()
            }
        })
    }

    //------------------------------ QR ---------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated: ")

        displayToast()
    }

    fun scanFromFragment() {
        Log.d(TAG, "scanFromFragment: ")
        IntentIntegrator.forSupportFragment(this).setOrientationLocked(false).initiateScan()
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
                binding.recipientAddressEv.setText(result.contents)
            }
        }
    }
}
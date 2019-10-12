package ai.tomorrow.tokenbox.send

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.databinding.FragmentSendEthBinding
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger


const val GAS_LIMIT_MIN = 11000
const val GAS_LIMIT_MAX = 31000


class SendEthFragment : Fragment() {

    private val TAG = "SendEthFragment"

    private lateinit var binding: FragmentSendEthBinding
    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))
    var gasCount = 21000
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var uiHandler = Handler()
    private lateinit var gasPriceWei: BigInteger

    private var toast: String? = null

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
                binding.recipientAddressEv.setText(result.contents)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSendEthBinding.inflate(inflater, container, false)
        val balanceString = SendEthFragmentArgs.fromBundle(arguments!!).balance

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val keystorePath =
            sharedPreferences.getString(getString(R.string.wallet_keystore_path), "") ?: ""
        val password = sharedPreferences.getString(getString(R.string.wallet_password), "") ?: ""
        val myAddress = sharedPreferences.getString(getString(R.string.wallet_address), "") ?: ""

        val balance = balanceString.split(" ")[0].toBigDecimal()
        val balanceWei = Convert.toWei(balance, Convert.Unit.ETHER).toBigInteger()


        uiScope.launch {
            withContext(Dispatchers.IO) {
                val gasprice = web3j.ethGasPrice().send().gasPrice
                uiHandler.post {
                    gasPriceWei = gasprice
                    binding.gasPriceTV.text = gasPriceWei.toString()
                }
            }
        }


        setupWidgets(balanceString, balanceWei, password, keystorePath, myAddress)

        binding.scanIv.setOnClickListener {
            scanFromFragment()
        }


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


        binding.sendBtn.setOnClickListener {
            Log.d(TAG, "sendBtn clicked")


            uiScope.launch {
                withContext(Dispatchers.IO) {
                    val toAddress = binding.recipientAddressEv.text.toString()

                    val amount = binding.amountEv.text.toString().toBigDecimal()

                    val amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
                    val gasLimitBigInteger = gasCount.toBigInteger()
                    val costWei = amountWei.add(gasPriceWei.multiply(gasLimitBigInteger))

                    if (costWei > balanceWei) {
                        Log.d(TAG, "costWei > balanceWei")
                        uiHandler.post {
                            Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        val transactionHash = sendTransaction(
                            password,
                            keystorePath,
                            myAddress,
                            gasLimitBigInteger,
                            toAddress,
                            amountWei
                        )

                        uiHandler.post {
                            if (transactionHash != null) {
                                Log.d(TAG, "You have successfully send a transaction!")
                                Toast.makeText(
                                    context,
                                    "You have successfully send a transaction!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                it.findNavController().navigateUp()
                            } else {
                                Log.d(TAG, "Transaction failed!")
                                Toast.makeText(context, "Transaction failed!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }

            }
        }

        binding.backBtn.setOnClickListener {
            it.findNavController().navigateUp()
        }
    }

    private fun sendTransaction(
        password: String,
        keystorePath: String,
        myAddress: String,
        gasLimitBigInteger: BigInteger,
        toAddress: String,
        amountWei: BigInteger?
    ): String? {
        val credentials = WalletUtils.loadCredentials(password, keystorePath)
        val ethGetTransactionCount = web3j.ethGetTransactionCount(
            myAddress, DefaultBlockParameterName.LATEST
        ).send()
        val nonce = ethGetTransactionCount.transactionCount


        val rawTransaction = RawTransaction.createEtherTransaction(
            nonce,
            gasPriceWei,
            gasLimitBigInteger,
            toAddress,
            amountWei
        )
        val signedMessage =
            TransactionEncoder.signMessage(rawTransaction, credentials)

        val hexValue = Numeric.toHexString(signedMessage)
        val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

        val transactionHash = ethSendTransaction.transactionHash
        return transactionHash
    }


}
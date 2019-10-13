package ai.tomorrow.tokenbox.send

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.HistoryDao
import ai.tomorrow.tokenbox.data.HistoryDatabase
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
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Timestamp
import java.util.*


const val GAS_LIMIT_MIN = 11000
const val GAS_LIMIT_MAX = 31000


class SendEthFragment : Fragment() {

    private val TAG = "SendEthFragment"

    private lateinit var binding: FragmentSendEthBinding
    private var toast: String? = null

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))

    // cototine
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var uiHandler = Handler()

    // vars
    var gasCount = 21000
    private lateinit var gasPriceWei: BigInteger

    //
    private lateinit var database: HistoryDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentSendEthBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application
        database = HistoryDatabase.getInstance(application).historyDao

        // get balance from bundle
        val balanceString = SendEthFragmentArgs.fromBundle(requireNotNull(arguments)).balance
        val balance = balanceString.split(" ")[0].toBigDecimal()
        val balanceWei = Convert.toWei(balance, Convert.Unit.ETHER).toBigInteger()

        // get wallet info from preference
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val keystorePath =
            sharedPreferences.getString(getString(R.string.wallet_keystore_path), "") ?: ""
        val password = sharedPreferences.getString(getString(R.string.wallet_password), "") ?: ""
        val myAddress = sharedPreferences.getString(getString(R.string.wallet_address), "") ?: ""

        // get gas price
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val gasprice = web3j.ethGasPrice().send().gasPrice
                uiHandler.post {
                    gasPriceWei = gasprice
                    val gasPriceShow = "${gasPriceWei.toBigDecimal().divide(BigDecimal("1000000000"))} GWEI"

                    binding.gasPriceTV.text = gasPriceShow
                }
            }
        }

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

            uiScope.launch {
                withContext(Dispatchers.IO) {
                    // get transaction info
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
                        makeTransaction(
                            password,
                            keystorePath,
                            myAddress,
                            gasLimitBigInteger,
                            toAddress,
                            amountWei,
                            it
                        )
                    }
                }
            }
        }

        // back button
        binding.backBtn.setOnClickListener {
            it.findNavController().navigateUp()
        }
    }

    private fun makeTransaction(
        password: String,
        keystorePath: String,
        myAddress: String,
        gasLimitBigInteger: BigInteger,
        toAddress: String,
        amountWei: BigInteger?,
        it: View
    ): Boolean {
        // sendTransaction
        val transactionHash = sendTransaction(
            password,
            keystorePath,
            myAddress,
            gasLimitBigInteger,
            toAddress,
            amountWei
        )

        if (!transactionHash.isNullOrEmpty()){
            Log.d(TAG, "You have successfully send a transaction!")
            val currentTimestamp = Timestamp(Date().time).time

            val pendingHistory = DatabaseHistory(
                0L, currentTimestamp, transactionHash, 0L, "", 0, myAddress, toAddress,
                amountWei.toString(), "", "", 2, 0L, 0L, 0L, myAddress
            )
            database.insert(pendingHistory)

            uiHandler.post{
                Toast.makeText(
                    context,
                    "You have successfully send a transaction!",
                    Toast.LENGTH_SHORT
                ).show()
                it.findNavController().navigateUp()
            }
            return true
        } else{
            uiHandler.post {
                Log.d(TAG, "Transaction failed!")
                Toast.makeText(context, "Transaction failed!", Toast.LENGTH_SHORT)
                    .show()
            }
            return false
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
        Log.d(TAG, "sendTransaction: ")

        // get transaction message
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

        // sign
        val signedMessage =
            TransactionEncoder.signMessage(rawTransaction, credentials)

        val hexValue = Numeric.toHexString(signedMessage)
        val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

        val transactionHash = ethSendTransaction.transactionHash
        return transactionHash
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
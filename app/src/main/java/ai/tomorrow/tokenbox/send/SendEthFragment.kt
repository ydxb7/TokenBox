package ai.tomorrow.tokenbox.send

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.databinding.FragmentSendEthBinding
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


const val GAS_LIMIT_MIN = 11000
const val GAS_LIMIT_MAX = 31000


class SendEthFragment : Fragment() {

    private val TAG = "SendEthFragment"

    private lateinit var binding: FragmentSendEthBinding
    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))
    var gasCount = 21000
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var uiHandler = Handler()

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
                binding.gasTV.text = gasCount.toString()
            }
        })


        binding.sendBtn.setOnClickListener {
            Log.d(TAG, "sendBtn clicked")


            uiScope.launch {
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "sendBtn clicked 222")
                    val gasPriceWei = web3j.ethGasPrice().send().gasPrice
                    val toAddress = binding.recipientAddressEv.text.toString()

                    val amount = binding.amountEv.text.toString().toBigDecimal()

                    val amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
                    val gasLimitBigInteger = gasCount.toBigInteger()
                    val costWei = amountWei.add(gasPriceWei.multiply(gasLimitBigInteger))

                    Log.d(TAG, "sendBtn clicked 333")
                    if (costWei > balanceWei) {
                        Log.d(TAG, "sendBtn clicked 444")
                        Log.d(TAG, "sendBtn costWei = $costWei, costWei = $costWei")
                        Log.d(TAG, "sendBtn costWei = $costWei, balanceWei = $balanceWei")
                        Log.d(TAG, "sendBtn amount = $amount, amountWei = $amountWei")
                        Log.d(TAG, "sendBtn costWei = $costWei, gasLimitBigInteger = $gasLimitBigInteger")
                        Log.d(TAG, "sendBtn costWei = $costWei, gasPriceWei = $gasPriceWei")
                        uiHandler.post {
                            Log.d(TAG, "sendBtn clicked 4441111111")
                            Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d(TAG, "sendBtn clicked 555")
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

//            }


        }








        binding.backBtn.setOnClickListener {
            it.findNavController().navigateUp()
        }

        return binding.root
    }


}
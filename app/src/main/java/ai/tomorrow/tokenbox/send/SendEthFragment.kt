package ai.tomorrow.tokenbox.send

import ai.tomorrow.tokenbox.databinding.FragmentSendEthBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert


const val GAS_LIMIT_MIN = 11000
const val GAS_LIMIT_MAX = 31000


class SendEthFragment : Fragment() {

    private val TAG = "SendEthFragment"

    private lateinit var binding: FragmentSendEthBinding
    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))
    var gasCount = 21000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSendEthBinding.inflate(inflater, container, false)
        val balanceString = SendEthFragmentArgs.fromBundle(arguments!!).balance
        val keystorePath = SendEthFragmentArgs.fromBundle(arguments!!).keystorePath

        val balance = balanceString.split(" ")[0].toBigDecimal()
        val balanceWei = Convert.toWei(balance, Convert.Unit.ETHER).toBigInteger()

        val gasPriceWei = web3j.ethGasPrice().send().gasPrice

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
                gasCount = 100000 + GAS_LIMIT_MIN
            }
        })


        binding.sendBtn.setOnClickListener {
            Log.d(TAG, "sendBtn clicked")

            val address = binding.recipientAddressEv.text.toString()

            val amount = binding.amountEv.text.toString().toBigDecimal()

            val amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
            val gasLimitWei = gasCount.toBigInteger()
            val costWei = amountWei.add(gasPriceWei.multiply(gasLimitWei))

            if (costWei < balanceWei) {
                Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT)
            } else {
                val credentials = WalletUtils.loadCredentials("", keystorePath)
            }


        }








        binding.backBtn.setOnClickListener {
            it.findNavController().navigateUp()
        }

        return binding.root
    }


}
package ai.tomorrow.tokenbox.send

import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.getDatabase
import ai.tomorrow.tokenbox.datasource.WalletDataSource
import android.app.Application
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
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
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject


class SendTransactionViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val TAG = "ImportWalletViewModel"

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))
    private val database = getDatabase(application).transactionDao
    private val walletDataSource = WalletDataSource()


    // cototine
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var uiHandler = Handler()

    // vars
    private val _gasPriceWei = MutableLiveData<BigInteger>()
    val gasPriceWei: LiveData<BigInteger>
        get() = _gasPriceWei

    private val _gasPriceShow = MutableLiveData<String>()
    val gasPriceShow: LiveData<String>
        get() = _gasPriceShow

    init {
        // get gas price
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val temp = web3j.ethGasPrice().send().gasPrice
                uiHandler.post {
                    _gasPriceWei.value = temp
                    _gasPriceShow.value =
                        "${temp.toBigDecimal().divide(BigDecimal("1000000000"))} GWEI"
                }
            }
        }
    }

    fun checkValid(
        amountWei: BigInteger,
        balanceWei: BigInteger?,
        gasLimitBigInteger: BigInteger
    ): Boolean {
        val costWei = amountWei.add(requireNotNull(gasPriceWei.value).multiply(gasLimitBigInteger))
        return costWei <= balanceWei
    }

    fun makeTransaction(
        password: String,
        keystorePath: String,
        myAddress: String,
        gasLimitBigInteger: BigInteger,
        toAddress: String,
        amountWei: BigInteger?,
        it: View
    ) {
        // sendTransaction
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val transactionHash = walletDataSource.sendTransaction(
                    password,
                    keystorePath,
                    myAddress,
                    gasLimitBigInteger,
                    toAddress,
                    amountWei,
                    gasPriceWei.value
                )

                if (!transactionHash.isNullOrEmpty()) {
                    Log.d(TAG, "You have successfully send a transaction!")
                    val currentTimestamp = Timestamp(Date().time).time

                    val pendingHistory = DatabaseHistory(
                        0L, currentTimestamp, transactionHash, 0L, "", 0, myAddress, toAddress,
                        amountWei.toString(), "", "", 2, 0L, 0L, 0L, myAddress
                    )
                    database.insert(pendingHistory)

                    uiHandler.post {
                        Toast.makeText(
                            application,
                            "You have successfully send a transaction!",
                            Toast.LENGTH_SHORT
                        ).show()
                        it.findNavController().navigateUp()
                    }
                } else {
                    uiHandler.post {
                        Log.d(TAG, "Transaction failed!")
                        Toast.makeText(application, "Transaction failed!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    }

//    private suspend fun sendTransaction(
//        password: String,
//        keystorePath: String,
//        myAddress: String,
//        gasLimitBigInteger: BigInteger,
//        toAddress: String,
//        amountWei: BigInteger?
//    ): String? {
//        Log.d(TAG, "sendTransaction: ")
//
//        // get transaction message
//        val credentials = WalletUtils.loadCredentials(password, keystorePath)
//        val ethGetTransactionCount = web3j.ethGetTransactionCount(
//            myAddress, DefaultBlockParameterName.LATEST
//        ).send()
//        val nonce = ethGetTransactionCount.transactionCount
//
//        val rawTransaction = RawTransaction.createEtherTransaction(
//            nonce,
//            gasPriceWei.value,
//            gasLimitBigInteger,
//            toAddress,
//            amountWei
//        )
//
//        // sign
//        val signedMessage =
//            TransactionEncoder.signMessage(rawTransaction, credentials)
//
//        val hexValue = Numeric.toHexString(signedMessage)
//        val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
//
//        val transactionHash = ethSendTransaction.transactionHash
//        return transactionHash
//    }

}
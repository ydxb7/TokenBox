package ai.tomorrow.tokenbox.send

import ai.tomorrow.tokenbox.repository.WalletRepository
import ai.tomorrow.tokenbox.repository.Web3jRepository
import ai.tomorrow.tokenbox.utils.Result
import android.app.Application
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.math.BigInteger


class SendTransactionViewModel(
    private val application: Application
) : ViewModel(), KoinComponent {

    private val TAG = "ImportWalletViewModel"

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))
    //    private val database = getDatabase(application).transactionDao
//    private val transactionRepository : TransactionRepository by inject()
    private val web3jRepository: Web3jRepository by inject()
    private val walletRepository: WalletRepository by inject()


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

    private val _navigateUp = MutableLiveData<Boolean>()
    val navigateUp: LiveData<Boolean>
        get() = _navigateUp

    fun navagationUpDone() {
        _navigateUp.value = false
    }

    val wallet = walletRepository.getWalletFromPreference()

    init {
        // get gas price
        uiScope.launch {

            web3jRepository.getGasPrice {
                when (it) {
                    is Result.Success -> {
                        postOnUiThead {
                            _gasPriceWei.value = it.value
                            _gasPriceShow.value =
                                "${it.value.toBigDecimal().divide(BigDecimal("1000000000"))} GWEI"
                        }
                    }
                    is Result.Failure -> {
                        postOnUiThead {
                            displayToast("Failed to get gas price.")
                        }
                    }
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
        toAddress: String,
        gasLimitBigInteger: BigInteger,
        amountWei: BigInteger
    ) {

        try {
            uiScope.launch {
                web3jRepository.sendTransaction(
                    wallet, toAddress, requireNotNull(gasPriceWei.value),
                    gasLimitBigInteger, amountWei
                ) {
                    when (it) {
                        is Result.Success -> {
                            displayToast("You have successfully send a transaction!")
                            postOnUiThead {
                                _navigateUp.value = true
                            }
                        }
                        is Result.Failure -> displayToast("Transaction failed!")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }

    private fun displayToast(message: String) {
        postOnUiThead {
            Toast.makeText(
                application,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun postOnUiThead(callback: () -> Unit) {
        uiHandler.post {
            callback.invoke()
        }
    }
}
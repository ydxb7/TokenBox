package ai.tomorrow.tokenbox.send

import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.datasource.WalletDataSource
import ai.tomorrow.tokenbox.repository.TransactionRepository
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
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Timestamp
import java.util.*


class SendTransactionViewModel(
    private val application: Application
) : ViewModel() {

    private val TAG = "ImportWalletViewModel"

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))
    //    private val database = getDatabase(application).transactionDao
    private val repository = TransactionRepository(application)


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

    fun navagationUpDone(){
        _navigateUp.value = false
    }


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
        transactionModel: WalletDataSource.TransactionModel
    ) {

        uiScope.launch {
            withContext(Dispatchers.IO) {
                repository.sendTransaction(transactionModel) {
                    when (it) {
                        is Result.Success -> {
                            displayToast("You have successfully send a transaction!")
                            _navigateUp.value = true
                        }
                        is Result.Failure -> displayToast("Transaction failed!")
                    }
                }
            }
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
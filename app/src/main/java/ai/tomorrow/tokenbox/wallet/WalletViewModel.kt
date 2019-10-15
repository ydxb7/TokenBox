package ai.tomorrow.tokenbox.wallet

import ai.tomorrow.tokenbox.repository.TransactionRepository
import ai.tomorrow.tokenbox.repository.WalletRepository
import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal

const val UPDATE_FREQUENCY = 5000L
const val API_KEY_TOKEN = "ZBE4XGYMYQ1R164QY3VY4S5TFFGHRYNEEI"

class WalletViewModel(
    private val application: Application
) : ViewModel(), KoinComponent {

    private val TAG = "WalletViewModel"

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))
    private val walletRepository: WalletRepository by inject()
    private val transactionRepository: TransactionRepository by inject()

    // coroutine
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // LiveData
    private val _currentAddress = MutableLiveData<String>()
    val currentAddress: LiveData<String>
        get() = _currentAddress

    private val _currentWalletName = MutableLiveData<String>()
    val currentWalletName: LiveData<String>
        get() = _currentWalletName

    val hasWallet = Transformations.map(currentAddress) {
        !currentAddress.value.isNullOrBlank()
    }


//    private val repository = delete_TransactionRepository(application)

    // poll data
    private var uiHandler = Handler()
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread
    private val refreshHistoryHandler = object : Runnable {
        override fun run() {
            Log.d(TAG, "refreshHistoryHandler, thread name: ${Thread.currentThread().name}")
            refreshAll()
            backgroundHandler.postDelayed(this, UPDATE_FREQUENCY)
        }
    }

    init {
        Log.d(TAG, "init")
        getCurrentWallet()

        refreshAll()

        backgroundThread = HandlerThread("backgroundHandler")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    // LiveData histories
    val databaseHistories = transactionRepository.histories

    val balance: LiveData<String> = Transformations.map(transactionRepository.balance) {
        if (it != null) {
            val ether = Convert.fromWei(BigDecimal(it.balance), Convert.Unit.ETHER).toFloat()
            "$ether ETH"
        } else {
            ""
        }
    }

    /**
     * This method will be invoked when wallet changes.
     * 1. remove all the data in the database
     * 2. get new data for database
     */
    fun resetDataset() {
        val address = _currentAddress.value
        if (address.isNullOrEmpty()) return
        uiScope.launch {
            transactionRepository.resetDbWithNewAddress(address)
        }
    }

    fun refreshAll() {
        Log.d(TAG, "refreshAll")
        val address = _currentAddress.value
        if (address.isNullOrEmpty()) return

        uiScope.launch {
            transactionRepository.refreshDb(address)
        }
    }

    fun getCurrentWallet() {
        Log.d(TAG, "getCurrentWallet and update LiveData _currentAddress")
        val wallet = walletRepository.getWalletFromPreference()

        _currentAddress.value = wallet.address
        _currentWalletName.value = wallet.name

        Log.d(TAG, "_currentAddress.value = ${_currentAddress.value}")
    }

    fun startPollingData() {
        Log.d(TAG, "startPollingData: make request every 30 second")
        backgroundHandler.post(refreshHistoryHandler)
    }

    fun stopPollingData() {
        Log.d(TAG, "stopPollingData: stop")
        backgroundHandler.removeCallbacks(refreshHistoryHandler)
    }

    override fun onCleared() {
        super.onCleared()
        stopPollingData()
        backgroundThread.stop()
    }

}
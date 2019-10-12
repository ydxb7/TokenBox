package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.HistoryDao
import ai.tomorrow.tokenbox.data.asDatabaseModel
import ai.tomorrow.tokenbox.network.EtherscanApi
import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.exceptions.ClientConnectionException
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal

const val UPDATE_FREQUENCY = 20000L
const val API_KEY_TOKEN = "ZBE4XGYMYQ1R164QY3VY4S5TFFGHRYNEEI"

class MainViewModel(
    private val application: Application,
    val database: HistoryDao
) : ViewModel() {

    private val TAG = "MainViewModel"

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))

    // coroutine
    val mutex = Mutex()
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // LiveData
    private val _currentAddress = MutableLiveData<String>()
    val currentAddress: LiveData<String>
        get() = _currentAddress

    private val _currentWalletName = MutableLiveData<String>()
    val currentWalletName: LiveData<String>
        get() = _currentWalletName

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String>
        get() = _balance

    val databaseHistories: LiveData<List<DatabaseHistory>> = database.getAllHistory()

    val hasWallet = Transformations.map(currentAddress) {
        !currentAddress.value.isNullOrBlank()
    }

    // poll data
    private var uiHandler = Handler()
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread
    private val backgroundThreadRunner = object : Runnable {
        override fun run() {
            Log.d(TAG, "backgroundThreadRunner, thread name: ${Thread.currentThread().name}")
            getBalance()
            refreshHistoryDatabaseFromNetwork()
            backgroundHandler.postDelayed(this, UPDATE_FREQUENCY)
        }
    }

    init {
        Log.d(TAG, "init")
        getCurrentWallet()

        backgroundThread = HandlerThread("backgroundHandler")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    /**
     * This method will be invoked when wallet changes.
     * 1. remove all the data in the database
     * 2. get new data for database
     */
    fun resetDataset() {
        Log.d(
            TAG,
            "XXX resetDataset: remove all data in the dataset and get new data for new address"
        )
        val address = _currentAddress.value
        if (address.isNullOrEmpty()) return
        uiScope.launch {
            withContext(Dispatchers.IO) {
                mutex.withLock {
                    database.clear()
                    fetchDataFromNetAndUpdateDatabase(address)
                }
            }
        }
    }

    fun refreshHistoryDatabaseFromNetwork() {
        Log.d(TAG, "refreshHistoryDatabaseFromNetwork")
        val address = _currentAddress.value
        if (address.isNullOrEmpty()) return

        uiScope.launch {
            withContext(Dispatchers.IO) {
                fetchDataFromNetAndUpdateDatabase(address)
            }
        }
    }

    private suspend fun fetchDataFromNetAndUpdateDatabase(address: String): Int {
        var getHistoryDeferred = EtherscanApi.retrofitService.getHistory(
            "account",
            "txlist",
            address,
            0,
            99999999,
            "desc",
            API_KEY_TOKEN
        )

        return try {
            var histories = getHistoryDeferred.await().result
            database.insertAll(*histories.asDatabaseModel(address))


            Log.d(TAG, "databaseHistories.value size = ${databaseHistories.value?.size}")
            Log.d(TAG, "databaseHistories.value = $databaseHistories")
        } catch (e: Exception) {
            Log.d(TAG, "Fail: ${e.message}")
        }
    }


    fun getCurrentWallet() {
        Log.d(TAG, "getCurrentWallet and update LiveData _currentAddress")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        _currentAddress.value =
            sharedPreferences.getString(application.getString(R.string.wallet_address), "") ?: ""
        _currentWalletName.value =
            sharedPreferences.getString(application.getString(R.string.wallet_name), "") ?: ""

        Log.d(TAG, "_currentAddress.value = ${_currentAddress.value}")
    }

    fun startPollingData() {
        Log.d(TAG, "startPollingData: make request every 30 second")
        backgroundHandler.post(backgroundThreadRunner)
    }

    fun stopPollingData() {
        Log.d(TAG, "stopPollingData: stop")
        backgroundHandler.removeCallbacks(backgroundThreadRunner)
    }


    private fun getBalance() {
        Log.d(TAG, "get balance, use web3j")

        val address = currentAddress.value

        if (!address.isNullOrEmpty()) {
            // send asynchronous requests to get balance
            try {
                val ethGetBalance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get()

                val wei = ethGetBalance.balance
                val ether = Convert.fromWei(BigDecimal(wei), Convert.Unit.ETHER).toFloat()

                uiHandler.post {
                    _balance.value = "$ether ETH"
                }
            } catch (e: ClientConnectionException) {
                Log.d(TAG, e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPollingData()
    }

}
package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.data.getDatabase
import ai.tomorrow.tokenbox.repository.Repository
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
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal

const val UPDATE_FREQUENCY = 5000L
const val API_KEY_TOKEN = "ZBE4XGYMYQ1R164QY3VY4S5TFFGHRYNEEI"

class MainViewModel(
    private val application: Application
) : ViewModel() {

    private val TAG = "MainViewModel"

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))

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

    private val database = getDatabase(application)

    private val repository = Repository(database)

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

        if (!currentAddress.value.isNullOrEmpty()){
            uiScope.launch {
                repository.refreshAll(requireNotNull(currentAddress.value))
            }
        }

        backgroundThread = HandlerThread("backgroundHandler")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    // LiveData histories
    val databaseHistories = repository.histories

    val balance: LiveData<String> = Transformations.map(repository.balance){
        if (it != null){
            val ether = Convert.fromWei(BigDecimal(it.balance), Convert.Unit.ETHER).toFloat()
            "$ether ETH"
        } else{
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
            repository.resetData(address)
        }
    }

    fun refreshAll() {
        Log.d(TAG, "refreshAll")
        val address = _currentAddress.value
        if (address.isNullOrEmpty()) return

        uiScope.launch {
            repository.refreshAll(address)
        }
    }

    fun getCurrentWallet() {
        Log.d(TAG, "getCurrentWallet and update LiveData _currentAddress")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        _currentAddress.value =
            sharedPreferences.getString(application.getString(R.string.wallet_address), "")
        _currentWalletName.value =
            sharedPreferences.getString(application.getString(R.string.wallet_name), "")

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
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
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.lang.Exception
import java.lang.Runnable
import java.math.BigDecimal

const val UPDATE_FREQUENCY = 5000L
const val API_KEY_TOKEN = "ZBE4XGYMYQ1R164QY3VY4S5TFFGHRYNEEI"

class MainViewModel(private val application: Application,
                    val database: HistoryDao) : ViewModel() {

    private val TAG = "MainViewModel"

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val _myAddress = MutableLiveData<String>()
    val myAddress: LiveData<String>
        get() = _myAddress

    private val _myWalletName = MutableLiveData<String>()
    val myWalletName: LiveData<String>
        get() = _myWalletName

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String>
        get() = _balance

    val databaseHistories : LiveData<List<DatabaseHistory>> = database.getAllHistory()
//    val databaseHistories: LiveData<List<DatabaseHistory>>
//        get() = _histories


    private var uiHandler = Handler()
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread
    private val backgroundThreadRunner = object : Runnable {
        override fun run() {
            Log.d(TAG, "backgroundThreadRunner, thread name: ${Thread.currentThread().name}" )
            getBalance()
            refreshHistoryDatabaseFromNetwork()
            backgroundHandler.postDelayed(this, UPDATE_FREQUENCY)
        }
    }


    init {
        Log.d(TAG, "init")
        getCurrentWallet()



//
//        uiScope.launch {
//            withContext(Dispatchers.IO){
//
//            }
//        }

        backgroundThread = HandlerThread("backgroundHandler")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    val mutex = Mutex()

    fun changeHistoryDataset(){
        Log.d(TAG, "XXX changeHistoryDataset: remove all data in the dataset and get new data for new address")
        val address = _myAddress.value
        if (address.isNullOrEmpty()) return
        uiScope.launch {
            withContext(Dispatchers.IO){
                mutex.withLock {
                    database.clear()
                    updateDataset(address)
                }
            }
        }
    }


    fun refreshHistoryDatabaseFromNetwork(){
        Log.d(TAG, "refreshHistoryDatabaseFromNetwork")
        val address = _myAddress.value
        if (address.isNullOrEmpty()) return

        uiScope.launch {
            withContext(Dispatchers.IO){
                updateDataset(address)
            }
        }
    }

    private suspend fun updateDataset(address: String): Int {
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
        Log.d(TAG, "getCurrentWallet")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        _myAddress.value =
            sharedPreferences.getString(application.getString(R.string.wallet_address), "") ?: ""
        _myWalletName.value =
            sharedPreferences.getString(application.getString(R.string.wallet_name), "") ?: ""

        Log.d(TAG, "_myAddress.value = ${_myAddress.value}")
    }

    fun startPollingBalance(){
        Log.d(TAG, "startPollingBalance: make request every 30 second")
        backgroundHandler.post(backgroundThreadRunner)
    }

    fun stopPollingBalance(){
        Log.d(TAG, "stopPollingBalance: stop")
        backgroundHandler.removeCallbacks(backgroundThreadRunner)
    }


    private fun getBalance() {
        Log.d(TAG, "get balance")

        val address = _myAddress.value
        // send asynchronous requests to get balance
        val ethGetBalance = web3j
            .ethGetBalance(address, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()

        val wei = ethGetBalance.balance
        val ether = Convert.fromWei(BigDecimal(wei), Convert.Unit.ETHER).toFloat()

        uiHandler.post {
            _balance.value = "$ether ETH"
        }
    }


    override fun onCleared() {
        super.onCleared()
        stopPollingBalance()
    }

}
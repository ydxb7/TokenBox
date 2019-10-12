package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.R
import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal

const val UPDATE_FREQUENCY = 30000L

class MainViewModel(private val application: Application) : ViewModel() {

    private val TAG = "MainViewModel"

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))



    private val _myAddress = MutableLiveData<String>()
    val myAddress: LiveData<String>
        get() = _myAddress

    private val _myWalletName = MutableLiveData<String>()
    val myWalletName: LiveData<String>
        get() = _myWalletName

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String>
        get() = _balance

    private var uiHandler = Handler()
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread
    private val backgroundThreadRunner = object : Runnable {
        override fun run() {
            Log.d(TAG, "backgroundThreadRunner, thread name: ${Thread.currentThread().name}" )
            getBalance()
            backgroundHandler.postDelayed(this, UPDATE_FREQUENCY)
        }
    }


    init {
        Log.d(TAG, "init")
        updateMyWallet()

        backgroundThread = HandlerThread("backgroundHandler")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }


    fun updateMyWallet() {
        Log.d(TAG, "updateMyWallet")
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
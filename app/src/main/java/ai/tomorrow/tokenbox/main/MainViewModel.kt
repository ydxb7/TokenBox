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

class MainViewModel (private val application: Application) : ViewModel() {

    private val TAG = "MainViewModel"

    private var uiHandler = Handler()
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread

    private val _myAddress = MutableLiveData<String>()
    val myAddress: LiveData<String>
        get() = _myAddress

    private val _myWalletName = MutableLiveData<String>()
    val myWalletName: LiveData<String>
        get() = _myWalletName


    init {
        Log.d(TAG, "init")
        updateMyWallet()
    }



    fun updateMyWallet(){
        Log.d(TAG, "updateMyWallet")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        _myAddress.value = sharedPreferences.getString(application.getString(R.string.wallet_address), "")?:""
        _myWalletName.value = sharedPreferences.getString(application.getString(R.string.wallet_name), "")?:""





        Log.d(TAG, "_myAddress.value = ${_myAddress.value}")


    }








}
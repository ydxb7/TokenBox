package ai.tomorrow.tokenbox.repository

import ai.tomorrow.tokenbox.data.DatabaseBalance
import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.TransactionDatabase
import ai.tomorrow.tokenbox.data.asDatabaseModel
import ai.tomorrow.tokenbox.main.API_KEY_TOKEN
import ai.tomorrow.tokenbox.network.EtherscanApi
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TransactionRepository (private val database: TransactionDatabase){

    val mutex = Mutex()
    val histories: LiveData<List<DatabaseHistory>> = database.transactionDao.getAllHistory()
    val balance: LiveData<DatabaseBalance> = database.transactionDao.getBalance()


    suspend fun refreshAll(address: String){
        withContext(Dispatchers.IO){
            refresh(address)
        }
    }

    suspend fun resetData(address: String){
        withContext(Dispatchers.IO) {
            mutex.withLock {
                database.transactionDao.clear()
                refresh(address)
            }
        }
    }

    private suspend fun refresh(address: String) {
        // refresh histories
        var getHistoryDeferred = EtherscanApi.retrofitService.getHistory(
            "account",
            "txlist",
            address,
            0,
            99999999,
            "desc",
            API_KEY_TOKEN
        )
        var histories = getHistoryDeferred.await().result
        database.transactionDao.insertAll(*histories.asDatabaseModel(address))

        // refresh balance
        val getBalanceDeferred = EtherscanApi.retrofitService.getBalance(
            "account",
            "balance",
            address,
            "latest",
            API_KEY_TOKEN
        )

        var balanceResponse = getBalanceDeferred.await()
        database.transactionDao.insertBalance(balanceResponse.asDatabaseModel())
    }



}
package ai.tomorrow.tokenbox.repository

import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.HistoryDatabase
import ai.tomorrow.tokenbox.data.asDatabaseModel
import ai.tomorrow.tokenbox.main.API_KEY_TOKEN
import ai.tomorrow.tokenbox.network.EtherscanApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class Repository (private val database: HistoryDatabase){

    val mutex = Mutex()
    val histories: LiveData<List<DatabaseHistory>> = database.historyDao.getAllHistory()


    suspend fun refreshHistories(address: String){
        withContext(Dispatchers.IO){
            refresh(address)
        }
    }

    suspend fun resetHistories(address: String){
        withContext(Dispatchers.IO) {
            mutex.withLock {
                database.historyDao.clear()
                refresh(address)
            }
        }
    }

    private suspend fun refresh(address: String) {
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
        database.historyDao.insertAll(*histories.asDatabaseModel(address))
    }


}
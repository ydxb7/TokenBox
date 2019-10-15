package ai.tomorrow.tokenbox.repository

import ai.tomorrow.tokenbox.data.asDatabaseModel
import ai.tomorrow.tokenbox.datasource.TransactionDatasource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TransactionRepository(val transactionDatasource: TransactionDatasource) {

    private val mutex = Mutex()

    val histories = transactionDatasource.histories
    val balance = transactionDatasource.balance


    suspend fun refreshDb(address: String) {
        withContext(Dispatchers.IO) {
            refresh(address)
        }
    }

    suspend fun resetDbWithNewAddress(address: String) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                transactionDatasource.clearHistoryData()
                refresh(address)
            }
        }
    }

    private suspend fun refresh(address: String) {
        val balanceString = transactionDatasource.fetchBalanceFromWeb(address)
        val history = transactionDatasource.fetchTransactionHistoryFromWeb(address)

        transactionDatasource.insertHistoryToDatabase(history.asDatabaseModel(address))
        transactionDatasource.insertBalanceToDb(balanceString)
    }
}
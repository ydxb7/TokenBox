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
    fun getHistory(rowId: Long) = transactionDatasource.getHistory(rowId)

    suspend fun refreshDb(address: String) {
        withContext(Dispatchers.IO) {
            refresh(address)
        }
    }

    suspend fun resetDbWithNewAddress(address: String) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                transactionDatasource.clearHistoryData()
                refreshDb(address)
            }
        }
    }

    private suspend fun refresh(address: String) {
        val balanceString = transactionDatasource.fetchBalanceFromWeb(address)
        val history = transactionDatasource.fetchTransactionHistoryFromWeb(address)

        transactionDatasource.insertHistoryToDatabase(history.asDatabaseModel(address))
        transactionDatasource.insertBalanceToDb(balanceString)
    }

    suspend fun newTransaction(address: String): List<Long> {
        val history = transactionDatasource.fetchTransactionHistoryFromWeb(address)

        val newIds =
            transactionDatasource.insertNewToDatabase(history.asDatabaseModel(address)).filter {
                it != -1L
            }

        return newIds
    }
}
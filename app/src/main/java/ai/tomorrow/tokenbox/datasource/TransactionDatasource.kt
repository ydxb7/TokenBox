package ai.tomorrow.tokenbox.datasource

import ai.tomorrow.tokenbox.data.DatabaseBalance
import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.NetworkHistory
import ai.tomorrow.tokenbox.data.getDatabase
import ai.tomorrow.tokenbox.main.API_KEY_TOKEN
import ai.tomorrow.tokenbox.network.EtherscanApi
import android.app.Application
import androidx.lifecycle.LiveData
import java.sql.Timestamp
import java.util.*

class TransactionDatasource(val application: Application) {

    val database = getDatabase(application).transactionDao

    val histories: LiveData<List<DatabaseHistory>> = database.getAllHistory()
    val balance: LiveData<DatabaseBalance> = database.getBalance()

    suspend fun insertHistoryToDatabase(histories: Array<DatabaseHistory>) {
        database.insertAll(*histories)
    }

    suspend fun insertBalanceToDb(balanceString: String) {
        database.insertBalance(DatabaseBalance(id = 1, balance = balanceString))
    }


    suspend fun clearHistoryData() {
        database.clear()
    }

    suspend fun fetchTransactionHistoryFromWeb(address: String): List<NetworkHistory> {
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
        return histories
    }

    suspend fun fetchBalanceFromWeb(address: String): String {
        // refresh balance
        val getBalanceDeferred = EtherscanApi.retrofitService.getBalance(
            "account",
            "balance",
            address,
            "latest",
            API_KEY_TOKEN
        )

        var balanceString = getBalanceDeferred.await().result
        return balanceString
    }
}
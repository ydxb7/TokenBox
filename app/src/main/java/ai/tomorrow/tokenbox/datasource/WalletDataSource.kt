package ai.tomorrow.tokenbox.datasource

import ai.tomorrow.tokenbox.data.DatabaseBalance
import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.asDatabaseModel
import ai.tomorrow.tokenbox.data.getDatabase
import ai.tomorrow.tokenbox.main.API_KEY_TOKEN
import ai.tomorrow.tokenbox.network.EtherscanApi
import ai.tomorrow.tokenbox.utils.Result
import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigInteger
import java.sql.Timestamp
import java.util.*

class WalletDataSource(application: Application) {
    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))

    private val mutex = Mutex()
    val database = getDatabase(application).transactionDao

    val histories: LiveData<List<DatabaseHistory>> = database.getAllHistory()
    val balance: LiveData<DatabaseBalance> = database.getBalance()

    data class TransactionModel(
        val password: String,
        val keystorePath: String,
        val myAddress: String,
        val gasLimitBigInteger: BigInteger,
        val toAddress: String,
        val amountWei: BigInteger?,
        val gasPriceWei: BigInteger?
    )

    fun sendTransaction(
        transactionModel: TransactionModel,
        callback: (Result<String, Exception>) -> Unit
    ) {

        // get transaction message
        val credentials =
            WalletUtils.loadCredentials(transactionModel.password, transactionModel.keystorePath)
        val ethGetTransactionCount = web3j.ethGetTransactionCount(
            transactionModel.myAddress, DefaultBlockParameterName.LATEST
        ).send()
        val nonce = ethGetTransactionCount.transactionCount

        val rawTransaction = RawTransaction.createEtherTransaction(
            nonce,
            transactionModel.gasPriceWei,
            transactionModel.gasLimitBigInteger,
            transactionModel.toAddress,
            transactionModel.amountWei
        )

        // sign
        val signedMessage =
            TransactionEncoder.signMessage(rawTransaction, credentials)

        val hexValue = Numeric.toHexString(signedMessage)

        try {
            val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
            val transactionHash = ethSendTransaction.transactionHash
            if (!transactionHash.isNullOrEmpty()) {
                insertPendingTransaction(transactionModel, transactionHash)
                callback.invoke(Result.Success(transactionHash.toString()))
            } else {
                callback.invoke(Result.Failure(IllegalStateException("transaction hash is null or empty")))
            }
        } catch (e: IOException) {
            callback.invoke(Result.Failure(e))
        }
    }

    private fun insertPendingTransaction(
        transactionModel: WalletDataSource.TransactionModel,
        hash: String
    ) {
        val currentTimestamp = Timestamp(Date().time).time

        val pendingHistory = DatabaseHistory(
            0L, currentTimestamp, hash, 0L, "", 0,
            transactionModel.myAddress, transactionModel.toAddress,
            transactionModel.amountWei.toString(), "", "", 2,
            0L, 0L, 0L, transactionModel.myAddress
        )
        database.insert(pendingHistory)
    }


    suspend fun refreshAll(address: String) {
        withContext(Dispatchers.IO) {
            refresh(address)
        }
    }

    suspend fun resetData(address: String) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                database.clear()
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
        database.insertAll(*histories.asDatabaseModel(address))

        // refresh balance
        val getBalanceDeferred = EtherscanApi.retrofitService.getBalance(
            "account",
            "balance",
            address,
            "latest",
            API_KEY_TOKEN
        )

        var balanceResponse = getBalanceDeferred.await()
        database.insertBalance(balanceResponse.asDatabaseModel())
    }
}
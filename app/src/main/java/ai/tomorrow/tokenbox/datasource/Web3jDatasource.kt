package ai.tomorrow.tokenbox.datasource

import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.Wallet
import ai.tomorrow.tokenbox.data.getDatabase
import ai.tomorrow.tokenbox.utils.Result
import android.app.Application
import android.util.Log
import kotlinx.coroutines.Dispatchers
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

class Web3jDatasource(application: Application) {

    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))
    val database = getDatabase(application).transactionDao

    suspend fun getGasPrice(callback: (Result<BigInteger, Exception>) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val gasPrice = web3j.ethGasPrice().send().gasPrice
                callback.invoke(Result.Success(gasPrice))
            } catch (e: IOException) {
                callback.invoke(Result.Failure(e))
            }
        }
    }

    suspend fun sendTransaction(
        wallet: Wallet,
        toAddress: String,
        gasPriceWei: BigInteger,
        gasLimitBigInteger: BigInteger,
        amountWei: BigInteger,
        callback: (Result<String, Exception>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            // get transaction message
            val credentials =
                WalletUtils.loadCredentials(wallet.password, wallet.keystorePath)
            val ethGetTransactionCount = web3j.ethGetTransactionCount(
                wallet.address, DefaultBlockParameterName.LATEST
            ).send()
            val nonce = ethGetTransactionCount.transactionCount

            val rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                gasPriceWei,
                gasLimitBigInteger,
                toAddress,
                amountWei
            )

            // sign
            val signedMessage =
                TransactionEncoder.signMessage(rawTransaction, credentials)

            val hexValue = Numeric.toHexString(signedMessage)
            val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
            val transactionHash = ethSendTransaction.transactionHash
            Log.d("XXX", "transactionHash = $transactionHash")

            try {
                if (!transactionHash.isNullOrEmpty()) {
                    callback.invoke(Result.Success(transactionHash.toString()))
                    insertPendingTransaction(
                        wallet.address, toAddress,
                        amountWei.toString(), transactionHash
                    )
                } else {
                    callback.invoke(Result.Failure(IllegalStateException("transaction hash is null or empty")))
                }
            } catch (e: IOException) {
                callback.invoke(Result.Failure(e))
            }
        }
    }

    private fun insertPendingTransaction(
        myAddress: String, toAddress: String, amountWei: String, hash: String
    ) {
        val currentTimestamp = Timestamp(Date().time).time / 1000

        val pendingHistory = DatabaseHistory(
            0L, currentTimestamp, hash, 0L, "", 0,
            myAddress, toAddress,
            amountWei, "", "", 2,
            0L, 0L, 0L, myAddress
        )
        database.insert(pendingHistory)
    }
}
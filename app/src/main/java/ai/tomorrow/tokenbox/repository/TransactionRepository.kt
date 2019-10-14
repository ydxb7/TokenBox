package ai.tomorrow.tokenbox.repository

import ai.tomorrow.tokenbox.datasource.WalletDataSource
import ai.tomorrow.tokenbox.utils.Result
import android.app.Application

class TransactionRepository(application: Application) {
//    val database = getDatabase(application).transactionDao

    val walletDataSource = WalletDataSource(application)


    val histories = walletDataSource.histories
    val balance = walletDataSource.balance

    fun sendTransaction(
        transactionModel: WalletDataSource.TransactionModel,
        callback: (Result<String, Exception>) -> Unit
    ) = walletDataSource.sendTransaction(
        transactionModel,
        callback
    )


}
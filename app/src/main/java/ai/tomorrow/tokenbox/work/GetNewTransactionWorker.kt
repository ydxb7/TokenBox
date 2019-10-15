package ai.tomorrow.tokenbox.work

import ai.tomorrow.tokenbox.repository.TransactionRepository
import ai.tomorrow.tokenbox.repository.WalletRepository
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.HttpException

class GetNewTransactionWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {

    companion object {
        const val WORK_NAME = "GetNewTransactionWorker"
    }

    val transactionRepository: TransactionRepository by inject()
    val walletRepository: WalletRepository by inject()

    var currentWallet = walletRepository.getWalletFromPreference()

    override suspend fun doWork(): Result {
        return try {
            val newWallet = walletRepository.getWalletFromPreference()
            if (currentWallet == newWallet) {
                val newIds = transactionRepository.newTransaction(currentWallet.address)
                // TODO 弹出通知

                Log.d(WORK_NAME, "XXX newIds = $newIds")
            } else {
                currentWallet = newWallet
            }
            Result.success()
        } catch (exception: HttpException) {
            Result.failure()
        }
    }


}
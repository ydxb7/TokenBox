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

    val wallet = walletRepository.getWalletFromPreference()

    override suspend fun doWork(): Payload {
        return try {
            val newIds = transactionRepository.newTransaction(wallet.address)

            // TODO 弹出通知

            Log.d("XXX", "newIds = $newIds")

            Payload(Result.SUCCESS)
        } catch (exception: HttpException) {
            Payload(Result.RETRY)
        }
    }


}
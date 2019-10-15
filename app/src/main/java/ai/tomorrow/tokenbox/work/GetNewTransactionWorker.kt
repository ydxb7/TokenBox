package ai.tomorrow.tokenbox.work

import ai.tomorrow.tokenbox.MainActivity
import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.repository.TransactionRepository
import ai.tomorrow.tokenbox.repository.WalletRepository
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.web3j.utils.Convert
import retrofit2.HttpException
import java.math.BigDecimal

class GetNewTransactionWorker(val appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {
    
    val transactionRepository: TransactionRepository by inject()
    val walletRepository: WalletRepository by inject()

    var currentWallet = walletRepository.getWalletFromPreference()

    override suspend fun doWork(): Result {
        return try {
            val newWallet = walletRepository.getWalletFromPreference()
            if (currentWallet == newWallet) {
                val newIds = transactionRepository.newTransaction(currentWallet.address)
                if (newIds.size > 0) {
                    var notificationBody = ""
                    for (rowId in newIds) {
                        val history = transactionRepository.getHistory(rowId)
                        val ether =
                            Convert.fromWei(BigDecimal(history.value), Convert.Unit.ETHER).toFloat()

                        notificationBody += "${history.from} send you $ether ETH \n"
                    }
                    sendNotification(appContext, notificationBody)
                }

                Log.d(WORK_NAME, "XXX newIds = $newIds")
            } else {
                currentWallet = newWallet
            }
            Result.success()
        } catch (exception: HttpException) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "GetNewTransactionWorker"

        private val PENDING_INTENT_ID = 3417
        private val NOTIFICATION_CHANNEL_ID = "reminder_notification_channel"
        private val NOTIFICATION_ID = 1138

        fun sendNotification(context: Context, notificationBody: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Primary",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(mChannel)
            }
            val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("You have an income")
                .setContentText(notificationBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
            }
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }

        private fun contentIntent(context: Context): PendingIntent {
            val startActivityIntent = Intent(context, MainActivity::class.java)

            return PendingIntent.getActivity(
                context,
                PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
package ai.tomorrow.tokenbox

import ai.tomorrow.tokenbox.work.GetNewTransactionWorker
import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

/**
 * Main Application
 */
class MainApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // start Koin context
        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(appModule)
        }

        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<GetNewTransactionWorker>(
            15,   // every 5 second
            TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(this)
            .enqueue(repeatingRequest)


//        WorkManager.getInstance().enqueueUniquePeriodicWork(
//            GetNewTransactionWorker.WORK_NAME,
//            ExistingPeriodicWorkPolicy.KEEP,
//            repeatingRequest
//        )
    }
}

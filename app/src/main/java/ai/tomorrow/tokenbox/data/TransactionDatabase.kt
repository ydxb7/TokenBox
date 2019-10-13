package ai.tomorrow.tokenbox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DatabaseHistory::class, DatabaseBalance::class], version = 2)
abstract class TransactionDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
}

private lateinit var INSTANCE: TransactionDatabase

fun getDatabase(context: Context): TransactionDatabase {
    synchronized(TransactionDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                TransactionDatabase::class.java,
                "history_database").fallbackToDestructiveMigration().build()
        }
    }
    return INSTANCE
}
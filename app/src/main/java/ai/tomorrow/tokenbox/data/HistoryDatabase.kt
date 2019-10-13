package ai.tomorrow.tokenbox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DatabaseHistory::class, DatabaseBalance::class], version = 2)
abstract class HistoryDatabase : RoomDatabase() {
    abstract val historyDao: HistoryDao
}

private lateinit var INSTANCE: HistoryDatabase

fun getDatabase(context: Context): HistoryDatabase {
    synchronized(HistoryDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                HistoryDatabase::class.java,
                "history_database").fallbackToDestructiveMigration().build()
        }
    }
    return INSTANCE
}
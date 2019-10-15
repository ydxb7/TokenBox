package ai.tomorrow.tokenbox.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionDao {

    @Query("select * from history_table ORDER BY timeStamp DESC")
    fun getAllHistory(): LiveData<List<DatabaseHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg histories: DatabaseHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNew(vararg histories: DatabaseHistory): List<Long>

    @Query("DELETE FROM history_table")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: DatabaseHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBalance(balance: DatabaseBalance)

    @Query("select * from balance_table")
    fun getBalance(): LiveData<DatabaseBalance>
}
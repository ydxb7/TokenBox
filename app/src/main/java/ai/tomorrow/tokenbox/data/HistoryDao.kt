package ai.tomorrow.tokenbox.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {

    @Query("select * from history_table")
    fun getAllHistory(): LiveData<List<DatabaseHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg histories: DatabaseHistory)

    @Query("DELETE FROM history_table")
    fun clear()

}
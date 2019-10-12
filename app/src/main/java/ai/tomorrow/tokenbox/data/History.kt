package ai.tomorrow.tokenbox.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class ResultResponse(
    val status: Int,
    val message: String,
    val result: List<History>
)

data class History(
    val blockNumber: Long,
    val timeStamp: Long,
    val hash: String,
    val nonce: Long,
    val blockHash: String,
    val transactionIndex: Int,
    val from: String,
    val to: String,
    val value: String,
    val gas: String,
    val gasPrice: String,
    val isError: Int,
    val cumulativeGasUsed: Long,
    val gasUsed: Long,
    val confirmations: Long
)

@Entity(tableName = "history_table")
data class DatabaseHistory constructor(
    val blockNumber: Long,
    val timeStamp: Long,
    @PrimaryKey
    val hash: String,
    val nonce: Long,
    val blockHash: String,
    val transactionIndex: Int,
    val from: String,
    val to: String,
    val value: String,
    val gas: String,
    val gasPrice: String,
    val isError: Int,
    val cumulativeGasUsed: Long,
    val gasUsed: Long,
    val confirmations: Long,
    val myAddress: String
)

fun List<DatabaseHistory>.asDomainModel(): List<History> {
    return map {
        History(
            blockNumber = it.blockNumber,
            timeStamp = it.timeStamp,
            hash = it.hash,
            nonce = it.nonce,
            blockHash = it.blockHash,
            transactionIndex = it.transactionIndex,
            from = it.from,
            to = it.to,
            value = it.value,
            gas = it.gas,
            gasPrice = it.gasPrice,
            isError = it.isError,
            cumulativeGasUsed = it.cumulativeGasUsed,
            gasUsed = it.gasUsed,
            confirmations = it.confirmations
        )
    }
}

fun List<History>.asDatabaseModel(myAddress: String): Array<DatabaseHistory> {
    return map {
        DatabaseHistory(
            blockNumber = it.blockNumber,
            timeStamp = it.timeStamp,
            hash = it.hash,
            nonce = it.nonce,
            blockHash = it.blockHash,
            transactionIndex = it.transactionIndex,
            from = it.from,
            to = it.to,
            value = it.value,
            gas = it.gas,
            gasPrice = it.gasPrice,
            isError = it.isError,
            cumulativeGasUsed = it.cumulativeGasUsed,
            gasUsed = it.gasUsed,
            confirmations = it.confirmations,
            myAddress = myAddress)
    }.toTypedArray()
}
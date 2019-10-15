package ai.tomorrow.tokenbox.repository

import ai.tomorrow.tokenbox.data.Wallet
import ai.tomorrow.tokenbox.datasource.Web3jDatasource
import ai.tomorrow.tokenbox.utils.Result
import java.math.BigInteger

class Web3jRepository(val web3jDatasource: Web3jDatasource) {

    suspend fun getGasPrice(callback: (Result<BigInteger, Exception>) -> Unit) =
        web3jDatasource.getGasPrice(callback)

    suspend fun sendTransaction(
        wallet: Wallet,
        toAddress: String,
        gasPriceWei: BigInteger,
        gasLimitBigInteger: BigInteger,
        amountWei: BigInteger,
        callback: (Result<String, Exception>) -> Unit
    ) = web3jDatasource.sendTransaction(
        wallet,
        toAddress,
        gasPriceWei,
        gasLimitBigInteger,
        amountWei,
        callback
    )

}
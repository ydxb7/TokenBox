package ai.tomorrow.tokenbox.datasource

import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger

class WalletDataSource {
    private val web3j = Web3j.build(HttpService("https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk"))

    fun sendTransaction(
        password: String,
        keystorePath: String,
        myAddress: String,
        gasLimitBigInteger: BigInteger,
        toAddress: String,
        amountWei: BigInteger?,
        gasPriceWei: BigInteger?
    ): String? {

        // get transaction message
        val credentials = WalletUtils.loadCredentials(password, keystorePath)
        val ethGetTransactionCount = web3j.ethGetTransactionCount(
            myAddress, DefaultBlockParameterName.LATEST
        ).send()
        val nonce = ethGetTransactionCount.transactionCount

        val rawTransaction = RawTransaction.createEtherTransaction(
            nonce,
            gasPriceWei,
            gasLimitBigInteger,
            toAddress,
            amountWei
        )

        // sign
        val signedMessage =
            TransactionEncoder.signMessage(rawTransaction, credentials)

        val hexValue = Numeric.toHexString(signedMessage)
        val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

        val transactionHash = ethSendTransaction.transactionHash
        return transactionHash
    }
}
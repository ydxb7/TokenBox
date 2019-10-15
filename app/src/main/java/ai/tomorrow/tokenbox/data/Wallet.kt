package ai.tomorrow.tokenbox.data

data class Wallet(
    val address: String,
    val name: String,
    val password: String,
    val passwordHint: String,
    val keystore: String,
    val keystorePath: String,
    val mnemonic: String,
    val privateKey: String
)
package ai.tomorrow.tokenbox

import ai.tomorrow.tokenbox.datasource.TransactionDatasource
import ai.tomorrow.tokenbox.datasource.WalletDatasource
import ai.tomorrow.tokenbox.datasource.Web3jDatasource
import ai.tomorrow.tokenbox.importwallet.ImportWalletViewModel
import ai.tomorrow.tokenbox.repository.TransactionRepository
import ai.tomorrow.tokenbox.repository.WalletRepository
import ai.tomorrow.tokenbox.repository.Web3jRepository
import ai.tomorrow.tokenbox.send.SendTransactionViewModel
import ai.tomorrow.tokenbox.wallet.WalletViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // MyViewModel ViewModel
    viewModel { WalletViewModel(get()) }
    viewModel { SendTransactionViewModel(get()) }
    viewModel { ImportWalletViewModel(get()) }

    single { TransactionDatasource(get()) }
    single { TransactionRepository(get()) }

    single { Web3jDatasource(get()) }
    single { Web3jRepository(get()) }

    single { WalletDatasource(get()) }
    single { WalletRepository(get()) }
}
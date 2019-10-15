package ai.tomorrow.tokenbox

import ai.tomorrow.tokenbox.importwallet.ImportWalletViewModel
import ai.tomorrow.tokenbox.send.SendTransactionViewModel
import ai.tomorrow.tokenbox.wallet.WalletViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    //    // single instance of HelloRepository
//    single<HelloRepository> { HelloRepositoryImpl() }
//
//    // Simple Presenter Factory
//    factory { MySimplePresenter(get()) }
//
//    // Simple Java Presenter
//    factory { MyJavaPresenter(get()) }
//
//    // scope for MyScopeActivity
//    scope(named<MyScopeActivity>()) {
//        // scoped MyScopePresenter instance
//        scoped { MyScopePresenter(get()) }
//    }

//    single<Application> {MainApplication()}

    // MyViewModel ViewModel
    viewModel { WalletViewModel(get()) }
    viewModel { SendTransactionViewModel(get()) }
    viewModel { ImportWalletViewModel(get()) }
}
package ai.tomorrow.tokenbox.importwallet

import ai.tomorrow.tokenjar.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.IndexOutOfBoundsException

const val IMPORT_KEYSTORE_PAGER_INDEX = 0
const val IMPORT_MNEMONIC_PAGER_INDEX = 1
const val IMPORT_PRIVATE_KEY_PAGER_INDEX = 2


class ImportWalletPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentCreators: Map<Int, () -> Fragment> = mapOf(
        IMPORT_KEYSTORE_PAGER_INDEX to { ImportWalletKeystoreFragment() },
        IMPORT_MNEMONIC_PAGER_INDEX to { ImportWalletMnemonicFragment() },
        IMPORT_PRIVATE_KEY_PAGER_INDEX to { ImportWalletPrivateKeyFragment() }
    )

    override fun getItemCount() = tabFragmentCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}
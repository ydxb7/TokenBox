package ai.tomorrow.tokenjar

import ai.tomorrow.tokenbox.databinding.FragmentImportWalletMnemonicBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ImportWalletMnemonicFragment : Fragment(){

    private val TAG = "ImportMnemonicFragment"

    private lateinit var binding: FragmentImportWalletMnemonicBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentImportWalletMnemonicBinding.inflate(inflater, container, false)

        return binding.root
    }

}
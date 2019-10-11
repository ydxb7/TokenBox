package ai.tomorrow.tokenjar

import ai.tomorrow.tokenbox.databinding.FragmentImportWalletKeystoreBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ImportWalletKeystoreFragment : Fragment(){

    private val TAG = "ImportKeystoreFragment"

    private lateinit var binding: FragmentImportWalletKeystoreBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentImportWalletKeystoreBinding.inflate(inflater, container, false)

        return binding.root
    }

}
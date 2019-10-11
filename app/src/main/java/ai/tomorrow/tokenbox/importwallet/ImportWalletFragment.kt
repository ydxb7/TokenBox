package ai.tomorrow.tokenjar

import ai.tomorrow.tokenbox.databinding.FragmentImportWalletBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class ImportWalletFragment : Fragment(){

    private val TAG = "ImportWalletFragment"

    private lateinit var binding: FragmentImportWalletBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentImportWalletBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener {
            it.findNavController().navigateUp()
        }

        return binding.root
    }

}
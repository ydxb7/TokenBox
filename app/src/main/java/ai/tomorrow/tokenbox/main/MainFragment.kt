package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.databinding.FragmentMainBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager

class MainFragment : Fragment() {

    private val TAG = "MainFragment"

    private lateinit var binding: FragmentMainBinding

    private var myAddress: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.addWalletBtn.setOnClickListener {
            Log.d(TAG, "addWalletBtn clicked")
            val direction =
                MainFragmentDirections.actionMainFragmentToImportWalletFragment()
            it.findNavController().navigate(direction)
        }

        binding.sendBtn.setOnClickListener {
            Log.d(TAG, "sendBtn clicked")
            val direction = MainFragmentDirections.actionMainFragmentToSendEthFragment()
            it.findNavController().navigate(direction)
        }



        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        myAddress = sharedPreferences.getString(getString(R.string.wallet_address), "")?:""
        binding.hasWallet = (myAddress != "")


    }



}
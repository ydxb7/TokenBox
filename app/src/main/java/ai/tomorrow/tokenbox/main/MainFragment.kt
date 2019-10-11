package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.databinding.FragmentMainBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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

        val application = requireNotNull(this.activity).application

        val viewModelFactory = MainViewModelFactory(application)

        val viewModel = ViewModelProviders.of(
                this, viewModelFactory).get(MainViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        viewModel.myAddress.observe(this, Observer {
            binding.hasWallet = it != null
        })








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
        binding.viewModel?.updateMyWallet()
    }



}
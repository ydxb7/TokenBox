package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.R
import ai.tomorrow.tokenbox.data.DatabaseHistory
import ai.tomorrow.tokenbox.data.HistoryDatabase
import ai.tomorrow.tokenbox.data.asDatabaseModel
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
        val database = HistoryDatabase.getInstance(application).historyDao

        val viewModelFactory = MainViewModelFactory(application, database)

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this



        val adapter = HistoryRecyclerViewAdapter(ArrayList())
        binding.historyRecyclerView.adapter = adapter


        viewModel.databaseHistories.observe(this, Observer {
            Log.d(TAG, "histories in the dataset = $it")
            Log.d(TAG, "histories in the dataset.size = ${it.size}")

            if (!it.isNullOrEmpty()){
                Log.d(TAG, "XXX first  = ${it[0].value}")
                Log.d(TAG, "XXX histories in the dataset.size = ${it.size}")
                adapter.setData(it)
            }

        })

        viewModel.myAddress.observe(this, Observer {
            if (it != null) {
                binding.hasWallet = true
                viewModel.startPollingBalance()
                viewModel.changeHistoryDataset()
            } else {
                binding.hasWallet = false
                viewModel.stopPollingBalance()
            }

        })


        binding.addWalletBtn.setOnClickListener {
            Log.d(TAG, "addWalletBtn clicked")
            val direction =
                MainFragmentDirections.actionMainFragmentToImportWalletFragment()
            it.findNavController().navigate(direction)
        }

        binding.sendBtn.setOnClickListener {
            Log.d(TAG, "sendBtn clicked")
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val keystorePath = sharedPreferences.getString(getString(R.string.wallet_keystore_path), "")?:""
            val direction = MainFragmentDirections.actionMainFragmentToSendEthFragment(
                viewModel.balance.value?:"0 ETH",
                keystorePath
                )
            it.findNavController().navigate(direction)
        }



        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        binding.viewModel?.getCurrentWallet()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        binding.viewModel?.stopPollingBalance()
    }
}
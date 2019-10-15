package ai.tomorrow.tokenbox.wallet

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


class WalletFragment : Fragment() {

    private val TAG = "WalletFragment"

    private lateinit var binding: FragmentMainBinding

    private var currentAddress: String = ""

    private lateinit var viewModel: WalletViewModel
    private lateinit var adapter: HistoryRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = FragmentMainBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application

        val viewModelFactory = WalletViewModelFactory(application)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(WalletViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        adapter = HistoryRecyclerViewAdapter(ArrayList())
        binding.historyRecyclerView.adapter = adapter

        setupWidgets()

        setupLiveData()

        return binding.root
    }

    private fun setupLiveData() {
        viewModel.databaseHistories.observe(this, Observer {
            Log.d(TAG, "histories in the dataset.size = ${it.size}")

            if (!it.isNullOrEmpty()) {
                adapter.setData(it)
            }
        })

        viewModel.currentAddress.observe(this, Observer { newAddress ->
            if (!newAddress.isNullOrEmpty() && newAddress != currentAddress) {
                currentAddress = newAddress
                viewModel.resetDataset()
            } else if (!newAddress.isNullOrEmpty()) {
                viewModel.startPollingData()
            } else {
                viewModel.stopPollingData()
            }
        })
    }

    private fun setupWidgets() {
        binding.addWalletBtn.setOnClickListener {
            Log.d(TAG, "addWalletBtn clicked")
            val direction =
                WalletFragmentDirections.actionMainFragmentToImportWalletFragment()
            it.findNavController().navigate(direction)
        }

        binding.addNewBtn.setOnClickListener {
            Log.d(TAG, "addNewBtn clicked")
            val direction =
                WalletFragmentDirections.actionMainFragmentToImportWalletFragment()
            it.findNavController().navigate(direction)
        }

        binding.sendBtn.setOnClickListener {
            Log.d(TAG, "sendBtn clicked")
            val direction = WalletFragmentDirections.actionMainFragmentToSendEthFragment(
                viewModel.balance.value ?: "0 ETH"
            )
            it.findNavController().navigate(direction)
        }


        binding.depositBtn.setOnClickListener {
            Log.d(TAG, "depositBtn clicked")

            val dialogFragment = QrcodeDialogFragment()

            dialogFragment.show(
                requireNotNull(fragmentManager),
                QrcodeDialogFragment::class.simpleName
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        // refresh wallet info in viewModel
        viewModel.getCurrentWallet()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        // stop updating data
        viewModel.stopPollingData()
    }
}
package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.QrcodeDialogFragment
import ai.tomorrow.tokenbox.data.HistoryDatabase
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder


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

            if (!it.isNullOrEmpty()) {
                Log.d(TAG, "XXX first  = ${it[0].value}")
                Log.d(TAG, "XXX histories in the dataset.size = ${it.size}")
                adapter.setData(it)
            }

        })

        viewModel.currentAddress.observe(this, Observer {
            if (it != null) {
                viewModel.startPollingData()
                viewModel.resetDataset()
            } else {
                viewModel.stopPollingData()
            }

        })


        binding.addWalletBtn.setOnClickListener {
            Log.d(TAG, "addWalletBtn clicked")
            val direction =
                MainFragmentDirections.actionMainFragmentToImportWalletFragment()
            it.findNavController().navigate(direction)
        }

        binding.addNewBtn.setOnClickListener {
            Log.d(TAG, "addNewBtn clicked")
            val direction =
                MainFragmentDirections.actionMainFragmentToImportWalletFragment()
            it.findNavController().navigate(direction)
        }

        binding.sendBtn.setOnClickListener {
            Log.d(TAG, "sendBtn clicked")
            val direction = MainFragmentDirections.actionMainFragmentToSendEthFragment(
                viewModel.balance.value ?: "0 ETH"
            )
            it.findNavController().navigate(direction)
        }


        binding.depositBtn.setOnClickListener {
            Log.d(TAG, "depositBtn clicked")

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val myAddress = sharedPreferences.getString(
                getString(ai.tomorrow.tokenbox.R.string.wallet_address),
                ""
            )

            if (!myAddress.isNullOrEmpty()) {
                try {
                    val multiFormatWriter = MultiFormatWriter()
                    val bitMatrix =
                        multiFormatWriter.encode(myAddress, BarcodeFormat.QR_CODE, 200, 200)
                    val barcodeEncoder = BarcodeEncoder()
                    val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                    val dialogFragment = QrcodeDialogFragment()

                    val bundle = Bundle()
                    bundle.putParcelable("qrcode", bitmap)

                    dialogFragment.arguments = bundle

                    dialogFragment.show(fragmentManager!!, QrcodeDialogFragment::class.simpleName)


                } catch (e: WriterException) {
                    Log.d(TAG, e.message)
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        // refresh wallet info in viewModel
        binding.viewModel?.getCurrentWallet()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        // stop updating data
        binding.viewModel?.stopPollingData()
    }
}
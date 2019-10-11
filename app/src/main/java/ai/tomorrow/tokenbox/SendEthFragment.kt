package ai.tomorrow.tokenbox

import ai.tomorrow.tokenbox.databinding.FragmentSendEthBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class SendEthFragment : Fragment(){

    private lateinit var binding: FragmentSendEthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSendEthBinding.inflate(inflater, container, false)


        binding.backBtn.setOnClickListener{
            it.findNavController().navigateUp()
        }

        return binding.root
    }


}
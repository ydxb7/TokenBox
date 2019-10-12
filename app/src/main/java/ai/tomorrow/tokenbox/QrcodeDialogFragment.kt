package ai.tomorrow.tokenbox

import ai.tomorrow.tokenbox.databinding.DialogFragmentQrBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

class QrcodeDialogFragment : DialogFragment() {

    private lateinit var binding: DialogFragmentQrBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogFragmentQrBinding.inflate(inflater, container, false)

        val qrcode = QrcodeDialogFragmentArgs.fromBundle(arguments!!).qrcode
        binding.qrIv.setImageBitmap(qrcode)


        return binding.root
    }

}
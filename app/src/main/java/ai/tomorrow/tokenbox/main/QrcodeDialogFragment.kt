package ai.tomorrow.tokenbox.main

import ai.tomorrow.tokenbox.R
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_qr.view.*

class QrcodeDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            inflater,
            R.layout.dialog_fragment_qr,
            container,
            false
        )

        val qrcode = arguments!!.getParcelable<Bitmap>("qrcode")
        binding.root.qrIv.setImageBitmap(qrcode)

        return binding.root
    }
}
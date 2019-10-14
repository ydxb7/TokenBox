package ai.tomorrow.tokenbox.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

fun createBitmapByAddress(myAddress: String?): Bitmap? {
    val multiFormatWriter = MultiFormatWriter()
    val bitMatrix =
        multiFormatWriter.encode(myAddress, BarcodeFormat.QR_CODE, 200, 200)
    val barcodeEncoder = BarcodeEncoder()
    val bitmap = barcodeEncoder.createBitmap(bitMatrix)
    return bitmap
}
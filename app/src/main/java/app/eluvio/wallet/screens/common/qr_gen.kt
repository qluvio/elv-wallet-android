package app.eluvio.wallet.screens.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.eluvio.wallet.util.logging.Log
import io.github.g0dkar.qrcode.QRCode
import io.reactivex.rxjava3.core.Single
import java.io.ByteArrayOutputStream

fun generateQrCode(url: String, size: Int): Single<Bitmap> {
    return Single.create {
        val bytes = ByteArrayOutputStream()
        val qr = QRCode(url)
        val rawData = qr.encode()
        val margin = 20 //(pixels)
        val cellSize = (size - margin) / rawData.size
        qr.render(margin = margin, cellSize = cellSize, rawData = rawData).writeImage(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.size())
        Log.d("QR generated for url: $url")
        it.onSuccess(bitmap)
    }
}

package app.eluvio.wallet.util.coil

import android.net.Uri
import app.eluvio.wallet.data.FabricUrl
import coil.map.Mapper
import coil.request.Options

/**
 * A [Mapper] that allows using [FabricUrl] directly in Coil requests.
 */
class FabricUrlMapper : Mapper<FabricUrl, Uri> {
    override fun map(data: FabricUrl, options: Options): Uri? {
        return data.url?.let { Uri.parse(it) }
    }
}

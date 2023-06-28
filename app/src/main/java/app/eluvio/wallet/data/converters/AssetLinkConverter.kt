package app.eluvio.wallet.data.converters

import app.eluvio.wallet.network.dto.AssetLinkDto
import app.eluvio.wallet.network.dto.FabricConfiguration

fun AssetLinkDto.toFullLink(config: FabricConfiguration): String {
    return "${config.endpoint}/$path"
}

package app.eluvio.wallet.data.converters

import app.eluvio.wallet.network.dto.AssetLinkDto
import app.eluvio.wallet.network.dto.FabricConfiguration
import app.eluvio.wallet.network.dto.MediaLinkDto

fun AssetLinkDto.toFullLink(config: FabricConfiguration): String {
    return "${config.endpoint}/s/${config.space}/${this.toLinkPath()}"
}

fun AssetLinkDto.toLinkPath(): String {
    val hash = dot.container
    val filePath = optionsPath.removePrefix("./")
    return "q/$hash/$filePath"
}

fun MediaLinkDto.toFullLink(config: FabricConfiguration): String? {
    return this.sources?.default?.toFullLink(config)
}

fun MediaLinkDto.toLinkPath(): String? {
    return this.sources?.default?.toLinkPath()
}

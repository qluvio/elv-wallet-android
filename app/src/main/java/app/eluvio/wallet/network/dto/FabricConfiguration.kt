package app.eluvio.wallet.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FabricConfiguration(
    @field:Json(name = "node_id") val nodeID: String,
    @field:Json(name = "network") val network: Network,
    @field:Json(name = "qspace") val qspace: QSpace,
    @field:Json(name = "fabric_version") val fabricVersion: String,
) {
    val endpoint: String = network.seedNodes.fabricApi.first()
    val space: String = qspace.names.first()
}

@JsonClass(generateAdapter = true)
data class Network(
    @field:Json(name = "seed_nodes") val seedNodes: SeedNodes,
    @field:Json(name = "api_versions") val apiVersions: List<Int>,
    @field:Json(name = "services") val services: Services
)

@JsonClass(generateAdapter = true)
data class SeedNodes(
    @field:Json(name = "fabric_api") val fabricApi: List<String>,
    @field:Json(name = "ethereum_api") val ethereumApi: List<String>
)

@JsonClass(generateAdapter = true)
data class Services(
    @field:Json(name = "authority_service") val authService: List<String>,
    @field:Json(name = "ethereum_api") val ethereumApi: List<String>,
    @field:Json(name = "fabric_api") val fabricApi: List<String>,
    @field:Json(name = "search") val search: List<String>,
)

@JsonClass(generateAdapter = true)
data class QSpace(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "version") val version: String,
    @field:Json(name = "type") val type: String,
    @field:Json(name = "ethereum") val ethereum: Ethereum,
    @field:Json(name = "names") val names: List<String>
)

@JsonClass(generateAdapter = true)
data class Ethereum(
    @field:Json(name = "network_id") val networkId: Int
)

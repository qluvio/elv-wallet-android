package app.eluvio.wallet.screens.property.mediagrid

data class MediaGridNavArgs(
    val propertyId: String,
    val sectionId: String? = null,
    // Either a "list" or a "collection" of media items
    val mediaContainerId: String? = null
)

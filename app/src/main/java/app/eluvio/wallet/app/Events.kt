package app.eluvio.wallet.app

interface Events {
    open class ToastMessage(val message: String) : Events

    object NetworkError : ToastMessage("Network error. Please try again.")

    object NftNotFound : ToastMessage("You don't own this NFT")
}

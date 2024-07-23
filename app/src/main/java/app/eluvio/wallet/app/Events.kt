package app.eluvio.wallet.app

interface Events {
    object NetworkError : Events {
        const val defaultMessage: String = "Network error. Please try again."
    }

    object NftNotFound : Events
}

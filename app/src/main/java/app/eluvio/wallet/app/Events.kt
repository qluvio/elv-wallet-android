package app.eluvio.wallet.app

sealed interface Events {
    object NetworkError : Events
    object NftNotFound : Events
}

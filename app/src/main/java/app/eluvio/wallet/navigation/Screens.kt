package app.eluvio.wallet.navigation

enum class Screens(val route: String) {
    // "Home" doesn't have a UI, it just decides where to go when the app is opened
    Home("/app/home"),
    // Select between Demo and Main
    EnvironmentSelection("/app/env_select"),
    // Show QR code and instructions for signing in
    SignIn("/app/sign_in"),
    // Where a logged in user lands and can start browsing media
    Dashboard("/app/dashboard"),
    ;
}

package app.eluvio.wallet.navigation

enum class Screens(val route: String) {
    // This one is special, it's not a real screen, it's just a way to go back to the previous screen.
    GoBack("I'm special!"),
    Home("/app/home"),
    EnvironmentSelection("/app/env_select"),
    SignIn("/app/sign_in"),
    Dashboard("/app/dashboard"),
    ;
}

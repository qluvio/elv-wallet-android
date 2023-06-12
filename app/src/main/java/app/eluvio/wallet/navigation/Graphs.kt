package app.eluvio.wallet.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

/**
 * Define navigation graphs in the app.
 */

@RootNavGraph(start = true)
@NavGraph
annotation class PreLaunchGraph(
    val start: Boolean = false
)

@RootNavGraph
@NavGraph
annotation class MainGraph(
    val start: Boolean = false
)

@RootNavGraph
@NavGraph
annotation class AuthFlowGraph(
    val start: Boolean = false
)

// No root, detached from main graph
@NavGraph
annotation class DashboardTabsGraph(
    val start: Boolean = false
)

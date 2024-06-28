package app.eluvio.wallet.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import app.eluvio.wallet.BuildConfig
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.screens.dashboard.discover.Discover
import app.eluvio.wallet.screens.dashboard.myitems.MyItems
import app.eluvio.wallet.screens.dashboard.profile.Profile
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.isKeyUpOf
import app.eluvio.wallet.util.logging.Log
import app.eluvio.wallet.util.rememberToaster
import coil.compose.AsyncImage
import coil.drawable.CrossfadeDrawable
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import io.reactivex.rxjava3.processors.PublishProcessor
import java.util.concurrent.TimeUnit

@MainGraph(start = true)
@Destination
@Composable
fun Dashboard() {
    val tabs = Tabs.AuthTabs
    var selectedTab by rememberSaveable { mutableStateOf(tabs.first()) }
    if (selectedTab !in tabs) {
        // This is a vestige of the never-used no-auth flow.
        selectedTab = tabs.first()
    }
    val selectedTabIndex = tabs.indexOf(selectedTab)
    var backgroundImage by rememberSaveable { mutableStateOf<String?>(null) }

//    val tabFocusRequesters = remember(tabs) { List(tabs.size) { FocusRequester() } }
    AnimatedBackground(url = backgroundImage)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        val contentFocusRequester = remember { FocusRequester() }
        var closedDrawerWidth by remember { mutableStateOf(0.dp) }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val density = LocalDensity.current
        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimBrush = Brush.horizontalGradient(listOf(Color.Black, Color.Transparent)),
            drawerContent = { drawerValue ->
                Column(
                    Modifier
                        .fillMaxHeight()
                        .padding(12.dp)
//                        .selectableGroup()
                        .onGloballyPositioned {
                            if (closedDrawerWidth == 0.dp) {
                                // Assume that the first callback we get will have the correct closed drawer width
                                with(density) {
                                    closedDrawerWidth = it.size.width.toDp()
                                }
                            }
                        }
                        .onKeyEvent {
                            Log.d("stav: onKey $it")
                            if (it.isKeyUpOf(Key.Back)) {
                                if (drawerValue == DrawerValue.Open) {
                                    contentFocusRequester.requestFocus()
                                    return@onKeyEvent true
                                }
                            } else if (it.key == Key.DirectionRight) {
                                // Something consumes the UP event, so we do this on either up or down.
                                contentFocusRequester.requestFocus()
                                return@onKeyEvent true
                            }
                            false
                        },
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedTabIndex == index
                        NavigationDrawerItem(
                            selected = selected,
                            onClick = {
                                selectedTab = tab
                                contentFocusRequester.requestFocus()
                            },
//                            modifier = Modifier.focusRequester(tabFocusRequesters[index]),
                            leadingContent = {
                                Icon(
                                    tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            },
                            content = { Text(text = stringResource(tab.title)) }
                        )
                    }
                }
            },
            content = {
                val modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(contentFocusRequester)
                    .padding(start = closedDrawerWidth)
                if (LocalInspectionMode.current) {
                    // Don't load real content in preview mode
                    Text(
                        text = "Dashboard page content",
                        textAlign = TextAlign.Center,
                        modifier = modifier.background(Color.Red.copy(alpha = 0.5f))
                    )
                } else {
                    TabContent(
                        selectedTab = selectedTab,
                        onBackgroundImageSet = { backgroundImage = it },
                        modifier = modifier
                    )
                }
            })
    }
}

@Composable
private fun TabContent(
    selectedTab: Tabs,
    onBackgroundImageSet: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedTab != Tabs.Discover) {
        onBackgroundImageSet(null)
    }
    AnimatedContent(
        targetState = selectedTab,
        label = "DashboardContent",
        modifier = modifier
    ) { tab ->
        when (tab) {
            Tabs.Discover -> Discover(onBackgroundImageSet)
            Tabs.MyItems -> MyItems()
//            Tabs.MyMedia -> MyMedia()
            Tabs.Profile -> Profile()
        }
    }
}

@Composable
private fun AnimatedBackground(url: String?, modifier: Modifier = Modifier) {
    val animationDuration = CrossfadeDrawable.DEFAULT_DURATION
    AnimatedContent(
        targetState = url,
        transitionSpec = {
            // Default transition spec has a scale animation and we don't want that
            (fadeIn(animationSpec = tween(animationDuration, delayMillis = 90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        },
        label = "bgImage"
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(it)
                .crossfade(animationDuration)
                .build(),
            contentScale = ContentScale.FillWidth,
            contentDescription = "background",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PrintVersionOnMultiClick(subject: PublishProcessor<Any>, clickThreshold: Int = 4) {
    val tripleClick by subject.buffer(3, TimeUnit.SECONDS, clickThreshold)
        .map { it.size >= clickThreshold }
        .distinctUntilChanged()
        .subscribeAsState(initial = false)
    if (tripleClick) {
        rememberToaster().toast(
            "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        )
    }
}

@Composable
@Preview(device = Devices.TV_720p)
private fun DashboardPreview() = EluvioThemePreview {
    Dashboard()
}

package app.eluvio.wallet.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowScope
import androidx.tv.material3.Text
import app.eluvio.wallet.BuildConfig
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.MainGraph
import app.eluvio.wallet.navigation.NavigationEvent
import app.eluvio.wallet.screens.common.AppLogo
import app.eluvio.wallet.screens.common.EluvioTab
import app.eluvio.wallet.screens.common.EluvioTabIndicator
import app.eluvio.wallet.screens.common.Overscan
import app.eluvio.wallet.screens.common.requestInitialFocus
import app.eluvio.wallet.screens.dashboard.discover.Discover
import app.eluvio.wallet.screens.dashboard.myitems.MyItems
import app.eluvio.wallet.screens.dashboard.mymedia.MyMedia
import app.eluvio.wallet.screens.dashboard.profile.Profile
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.theme.header_30
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

    val tabFocusRequesters = remember(tabs) { List(tabs.size) { FocusRequester() } }
    var topBarFocused by rememberSaveable { mutableStateOf(false) }
    val navigator = LocalNavigator.current

    AnimatedBackground(url = backgroundImage)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .onKeyEvent {
                // Capture back presses
                if (it.isKeyUpOf(Key.Back)) {
                    if (!topBarFocused) {
                        tabFocusRequesters[selectedTabIndex].requestFocus()
                    } else if (selectedTabIndex != 0) {
                        tabFocusRequesters[0].requestFocus()
                    } else {
                        navigator(NavigationEvent.GoBack)
                    }
                    return@onKeyEvent true
                }
                false
            }
    ) {
        TopBar(
            tabs,
            selectedTabIndex,
            tabFocusRequesters,
            onTabSelected = { tab ->
                selectedTab = tab
            },
            modifier = Modifier
                .onFocusChanged {
                    topBarFocused = it.hasFocus
                }
        )
        val modifier = Modifier.fillMaxSize()
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
        label = "DashboardContent"
    ) { tab ->
        when (tab) {
            Tabs.Discover -> Discover(onBackgroundImageSet)
            Tabs.MyItems -> MyItems()
            Tabs.MyMedia -> MyMedia()
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
private fun TopBar(
    tabs: List<Tabs>,
    selectedTabIndex: Int,
    tabFocusRequesters: List<FocusRequester>,
    onTabSelected: (Tabs) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(Overscan.defaultPadding(excludeBottom = true))
    ) {
        if (selectedTabIndex == 0) {
            // Kind of a hack, the Discover tab doesn't need a logo,
            // so we just assume it's the first tab
            Spacer(Modifier.weight(1f))
        } else {
            AppLogo(Modifier.weight(1f))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xff373737))
                .padding(5.dp)
                .align(Alignment.CenterVertically)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                contentColor = Color.White,
                indicator = { tabPositions, doesTabRowHaveFocus ->
                    EluvioTabIndicator(
                        selectedTabIndex,
                        tabPositions,
                        doesTabRowHaveFocus
                    )
                },
                modifier = Modifier
                    .requestInitialFocus()
                    .onFocusChanged {
                        if (it.hasFocus) {
                            // Manually restore focus to the selected tab.
                            // Required since we removed the FocusGroup wrapper
                            tabFocusRequesters[selectedTabIndex].requestFocus()
                        }
                    }
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = selectedTabIndex == index
                    val focusRequester = tabFocusRequesters[index]
                    DashboardTab(
                        tab,
                        selected = selected,
                        onFocus = {
                            if (!selected) {
                                onTabSelected(tab)
                                Log.v("Tab focused: $tab")
                            }
                        },
                        onClick = { focusManager.moveFocus(FocusDirection.Down) },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                    LaunchedEffect(selected) {
                        if (selected) {
                            // Manually focus self. This avoids focus issues when tabs change state.
                            Log.d("Tab $tab self-requesting focus.")
                            focusRequester.requestFocus()
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TabRowScope.DashboardTab(
    tab: Tabs,
    selected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subject = remember { PublishProcessor.create<Any>() }
    PrintVersionOnMultiClick(subject)

    EluvioTab(
        selected = selected,
        onFocus = onFocus,
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .onKeyEvent {
                if (tab == Tabs.Profile && it.isKeyUpOf(Key.DirectionRight)) {
                    subject.onNext(true)
                }
                false
            }
    ) {
        val icon = tab.icon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(tab.title),
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically)
            )
        } else {
            Text(
                text = stringResource(tab.title),
                // TODO: use standard font
                style = MaterialTheme.typography.header_30.copy(fontWeight = FontWeight.W600),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
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
private fun TopBarPreview() = EluvioThemePreview {
    TopBar(
        Tabs.entries,
        selectedTabIndex = 0,
        tabFocusRequesters = remember { List(Tabs.entries.size) { FocusRequester() } },
        onTabSelected = { },
    )
}

@Composable
@Preview(device = Devices.TV_720p)
private fun DashboardPreview() = EluvioThemePreview {
    Dashboard()
}

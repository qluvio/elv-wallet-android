### File template

There's a lot of boilerplate involved with creating a new Composable/ViewModel pair.
Use this [Template with multiple files](https://www.jetbrains.com/help/idea/templates-with-multiple-files.html) to generate the files for you.

```
package ${PACKAGE_NAME}

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import app.eluvio.wallet.navigation.NavigationCallback
import app.eluvio.wallet.theme.EluvioThemePreview
import app.eluvio.wallet.util.subscribeToState

@Composable
fun ${NAME}(navCallback: NavigationCallback) {
    hiltViewModel<${NAME}ViewModel>().subscribeToState(navCallback) { vm, state ->
        ${NAME}(state, navCallback)
    }
}

@Composable
private fun ${NAME}(state: ${NAME}ViewModel.State, navCallback: NavigationCallback) {

}

@Composable
@Preview(device = Devices.TV_720p)
private fun ${NAME}Preview() = EluvioThemePreview {
    ${NAME}(${NAME}ViewModel.State(), navCallback = { })
}
```

And create a Child Template File for the ViewModel

```
package ${PACKAGE_NAME}

import app.eluvio.wallet.app.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ${NAME}ViewModel @Inject constructor(
) : BaseViewModel<${NAME}ViewModel.State>(State()) {
    data class State(val tmp: Int = 0)
}
```

package app.eluvio.wallet.screens.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.eluvio.wallet.R
import app.eluvio.wallet.navigation.AuthFlowGraph
import app.eluvio.wallet.navigation.LocalNavigator
import app.eluvio.wallet.navigation.asPush
import app.eluvio.wallet.screens.common.TvButton
import app.eluvio.wallet.screens.common.requestInitialFocus
import app.eluvio.wallet.screens.destinations.SignInDestination
import app.eluvio.wallet.theme.header_53
import com.ramcosta.composedestinations.annotation.Destination

@AuthFlowGraph(start = true)
@Destination
@Composable
fun SignInPreamble(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.elv_logo),
            contentDescription = "Eluvio Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.header_53.copy(
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraLight
            )
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Media Wallet",
            style = MaterialTheme.typography.header_53.copy(fontSize = 44.sp)
        )

        val navigator = LocalNavigator.current
        Spacer(modifier = Modifier.height(30.dp))
        TvButton(
            stringResource(R.string.sign_in_button),
            onClick = { navigator(SignInDestination.asPush()) },
            modifier = Modifier.requestInitialFocus()
        )
    }
}

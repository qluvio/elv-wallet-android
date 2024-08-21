package app.eluvio.wallet.screens.nftdetail.legacy

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.screens.navArgs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

data class LegacyNftDetailArgs(
    val contractAddress: String,
    val tokenId: String,
    val marketplaceId: String? = null,
    val backLink: String? = null,
)

@Module
@InstallIn(ViewModelComponent::class)
object NavArgModule {
    @Provides
    fun SavedStateHandle.provide(): LegacyNftDetailArgs = navArgs()
}

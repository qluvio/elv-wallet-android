package app.eluvio.wallet.screens.nftdetail

import androidx.lifecycle.SavedStateHandle
import app.eluvio.wallet.screens.navArgs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

data class NftDetailNavArgs(
    val contractAddress: String,
    val tokenId: String,
)

@Module
@InstallIn(ViewModelComponent::class)
object NavArgModule {
    @Provides
    fun SavedStateHandle.provide(): NftDetailNavArgs = navArgs()
}

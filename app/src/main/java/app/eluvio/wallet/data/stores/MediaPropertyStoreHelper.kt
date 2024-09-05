package app.eluvio.wallet.data.stores

import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.data.entities.v2.MediaPageEntity
import app.eluvio.wallet.data.entities.v2.MediaPageSectionEntity
import app.eluvio.wallet.data.entities.v2.MediaPropertyEntity
import app.eluvio.wallet.data.entities.v2.SectionItemEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionContext
import io.reactivex.rxjava3.core.Flowable

/**
 * Convenience method that observes a hierarchy defined by [permissionContext].
 * The resolution order is as follows:
 * Property -> Page -> Section -> SectionItem -> MediaItem
 * Resolution will stop at the first null value.
 */
fun MediaPropertyStore.resolveContext(permissionContext: PermissionContext): Flowable<ResolvedContext> {
    val (propertyId, pageId, sectionId, sectionItemId, mediaItemId) = permissionContext
    return observeMediaProperty(propertyId, forceRefresh = false)
        .map { ResolvedContext(property = it) }
        .switchMap { observePage(pageId, context = it) }
        .switchMap { observeSection(sectionId, context = it) }
        .map { context ->
            val sectionItem = context.section?.items?.firstOrNull { it.id == sectionItemId }
            val mediaItem = sectionItem?.media?.takeIf { it.id == mediaItemId }
            context.copy(sectionItem = sectionItem, mediaItem = mediaItem)
        }
}

data class ResolvedContext(
    val property: MediaPropertyEntity,
    val page: MediaPageEntity? = null,
    val section: MediaPageSectionEntity? = null,
    val sectionItem: SectionItemEntity? = null,
    val mediaItem: MediaEntity? = null
)

private fun MediaPropertyStore.observePage(
    pageId: String?,
    context: ResolvedContext
): Flowable<ResolvedContext> {
    val mainPage = context.property.mainPage
    return when (pageId) {
        // No page. Stop resolution.
        null -> Flowable.just(context)

        mainPage?.id -> {
            // Short circuit. If we are interested the main page, we already have it
            Flowable.just(context.copy(page = mainPage))
        }

        else -> observePage(context.property, pageId, forceRefresh = false)
            .map { context.copy(page = it) }
    }
}

private fun MediaPropertyStore.observeSection(
    sectionId: String?,
    context: ResolvedContext
): Flowable<ResolvedContext> {
    return if (context.page == null || sectionId == null) {
        Flowable.just(context)
    } else {
        observeSections(
            context.property,
            context.page,
            sectionIds = listOf(sectionId),
            forceRefresh = false
        )
            .map { context.copy(section = it.firstOrNull()) }
    }
}

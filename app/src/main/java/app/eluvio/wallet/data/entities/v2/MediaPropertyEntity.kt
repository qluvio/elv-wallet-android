package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.data.entities.v2.permissions.EntityWithPermissions
import app.eluvio.wallet.data.entities.v2.permissions.PermissionsEntity
import app.eluvio.wallet.data.entities.v2.permissions.PermissionStatesEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class MediaPropertyEntity : RealmObject, EntityWithPermissions {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var headerLogo: String = ""
    var image: String = ""

    // Property can also include a list of pages besides the main page.
    // But the TV apps have no use for it currently.
    var mainPage: MediaPageEntity? = null

    var loginInfo: PropertyLoginInfoRealmEntity? = null

    @Ignore
    val loginProvider: LoginProviders
        get() = loginInfo?.loginProvider ?: LoginProviders.UNKNOWN

    var permissionStates = realmDictionaryOf<PermissionStatesEntity?>()

    @Ignore
    override var resolvedPermissions: PermissionsEntity? = null
    override var rawPermissions: PermissionsEntity? = null
    override val permissionChildren: List<EntityWithPermissions>
        get() = listOfNotNull(mainPage)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaPropertyEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (headerLogo != other.headerLogo) return false
        if (image != other.image) return false
        if (mainPage != other.mainPage) return false
        if (loginInfo != other.loginInfo) return false
        if (permissionStates != other.permissionStates) return false
        if (resolvedPermissions != other.resolvedPermissions) return false
        if (rawPermissions != other.rawPermissions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + headerLogo.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + (mainPage?.hashCode() ?: 0)
        result = 31 * result + (loginInfo?.hashCode() ?: 0)
        result = 31 * result + permissionStates.hashCode()
        result = 31 * result + (resolvedPermissions?.hashCode() ?: 0)
        result = 31 * result + (rawPermissions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaPropertyEntity(id='$id', name='$name', headerLogo='$headerLogo', image='$image', mainPage=$mainPage, loginInfo=$loginInfo, permissionStates=$permissionStates, resolvedPermissions=$resolvedPermissions, rawPermissions=$rawPermissions)"
    }

    // Index can't be saved as part of the PropertyEntity object because it will get overridden
    // when fetching a single property from the API.
    class PropertyOrderEntity : RealmObject {
        @PrimaryKey
        var propertyId: String = ""
        var index: Int = Int.MAX_VALUE

        override fun toString(): String {
            return "PropertyOrderEntity(propertyId='$propertyId', index=$index)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PropertyOrderEntity

            if (propertyId != other.propertyId) return false
            if (index != other.index) return false

            return true
        }

        override fun hashCode(): Int {
            var result = propertyId.hashCode()
            result = 31 * result + index
            return result
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @ElementsIntoSet
        fun provideEntities(): Set<KClass<out TypedRealmObject>> =
            setOf(MediaPropertyEntity::class, PropertyOrderEntity::class)
    }
}

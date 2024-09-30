package app.eluvio.wallet.data.entities.v2

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class PropertySearchFiltersEntity : RealmObject {
    @PrimaryKey
    var propertyId: String = ""

    var tags: RealmList<String> = realmListOf()
    var attributes = realmDictionaryOf<SearchFilterAttribute?>()

    var primaryFilter: SearchFilterAttribute? = null

    override fun toString(): String {
        return "PropertySearchFiltersEntity(propertyId='$propertyId', tags=$tags, attributes=$attributes, primaryFilter=$primaryFilter)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PropertySearchFiltersEntity

        if (propertyId != other.propertyId) return false
        if (tags != other.tags) return false
        if (attributes != other.attributes) return false
        if (primaryFilter != other.primaryFilter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = propertyId.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + attributes.hashCode()
        result = 31 * result + (primaryFilter?.hashCode() ?: 0)
        return result
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = PropertySearchFiltersEntity::class
    }
}

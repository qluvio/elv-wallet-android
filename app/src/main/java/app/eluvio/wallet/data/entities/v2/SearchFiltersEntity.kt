package app.eluvio.wallet.data.entities.v2

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.reflect.KClass

class SearchFiltersEntity : RealmObject {
    @PrimaryKey
    var propertyId: String = ""

    var tags: RealmList<String> = realmListOf()
    var attributes: RealmList<Attribute> = realmListOf()

    var primaryFilter: Attribute? = null
    var secondaryFilter: Attribute? = null

    class Attribute : EmbeddedRealmObject {
        var id: String = ""
        var title: String = ""
        var tags: RealmList<AttributeValue> = realmListOf()

        fun copy(): Attribute {
            val src = this
            return Attribute().apply {
                id = src.id
                title = src.title
                tags = src.tags
            }
        }
    }

    class AttributeValue : EmbeddedRealmObject {
        companion object {
            const val ALL = "All"
            fun from(tag: String) = AttributeValue().apply { value = tag }
        }

        var value: String = ""
        var nextFilterAttribute: String? = null
        var image: String? = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchFiltersEntity

        if (propertyId != other.propertyId) return false
        if (tags != other.tags) return false
        if (attributes != other.attributes) return false
        if (primaryFilter != other.primaryFilter) return false
        if (secondaryFilter != other.secondaryFilter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = propertyId.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + attributes.hashCode()
        result = 31 * result + (primaryFilter?.hashCode() ?: 0)
        result = 31 * result + (secondaryFilter?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SearchFiltersEntity(propertyId='$propertyId', tags=$tags, attributes=$attributes, primaryFilter=$primaryFilter, secondaryFilter=$secondaryFilter)"
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @ElementsIntoSet
        fun provideEntity(): Set<KClass<out TypedRealmObject>> =
            setOf(SearchFiltersEntity::class, Attribute::class, AttributeValue::class)
    }
}

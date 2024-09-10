package app.eluvio.wallet.data.entities.v2

import app.eluvio.wallet.data.entities.FabricUrlEntity
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
        var values: RealmList<AttributeValue> = realmListOf()

        fun copy(): Attribute {
            val src = this
            return Attribute().apply {
                id = src.id
                title = src.title
                values = src.values
            }
        }

        override fun toString(): String {
            return "Attribute(id='$id', title='$title', values=$values)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Attribute

            if (id != other.id) return false
            if (title != other.title) return false
            if (values != other.values) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + title.hashCode()
            result = 31 * result + values.hashCode()
            return result
        }
    }

    class AttributeValue : EmbeddedRealmObject {
        companion object {
            const val ALL = "All"
            fun from(value: String) = AttributeValue().apply { this.value = value }
        }

        var value: String = ""
        var nextFilterAttribute: String? = null
        var imageUrl: FabricUrlEntity? = null

        override fun toString(): String {
            return "AttributeValue(value='$value', nextFilterAttribute=$nextFilterAttribute, image=$imageUrl)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AttributeValue

            if (value != other.value) return false
            if (nextFilterAttribute != other.nextFilterAttribute) return false
            if (imageUrl != other.imageUrl) return false

            return true
        }

        override fun hashCode(): Int {
            var result = value.hashCode()
            result = 31 * result + (nextFilterAttribute?.hashCode() ?: 0)
            result = 31 * result + (imageUrl?.hashCode() ?: 0)
            return result
        }

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

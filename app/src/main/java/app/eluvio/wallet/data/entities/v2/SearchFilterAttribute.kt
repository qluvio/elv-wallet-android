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
import io.realm.kotlin.types.TypedRealmObject
import kotlin.reflect.KClass

class SearchFilterAttribute : EmbeddedRealmObject {
    var id: String = ""
    var title: String = ""
    var values: RealmList<Value> = realmListOf()

    fun copy(): SearchFilterAttribute {
        val src = this
        return SearchFilterAttribute().apply {
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

        other as SearchFilterAttribute

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

    class Value : EmbeddedRealmObject {
        companion object {
            const val ALL = "All"
            fun from(value: String) = Value().apply { this.value = value }
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

            other as Value

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

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @ElementsIntoSet
        fun provideEntity(): Set<KClass<out TypedRealmObject>> =
            setOf(
                SearchFilterAttribute::class,
                Value::class,
            )
    }
}

package app.eluvio.wallet.network.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.rawType
import java.lang.reflect.Type

/**
 * Fabric objects are sometimes returned as empty strings ("") instead of null.
 * This adapter will treat any type that is expected to be an object/array (not primitives/strings),
 * but doesn't have the expected token (object or array, respectively) as null.
 */
class FalsyObjectAdapter(
    private val delegate: JsonAdapter<Any>
) : JsonAdapter<Any>() {

    override fun fromJson(reader: JsonReader): Any? {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_ARRAY,
            JsonReader.Token.BEGIN_OBJECT -> delegate.fromJson(reader)

            // Hack to get around the server sending "" for Dates.
            JsonReader.Token.STRING -> {
                try {
                    delegate.fromJson(reader)
                } catch (e: JsonDataException) {
                    if (e.message?.contains("Not an RFC 3339 date") != true) {
                        reader.skipValue()
                    }
                    null
                }
            }

            else -> {
                reader.skipValue()
                null
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: Any?) {
        return delegate.toJson(writer, value)
    }

    /**
     * Wrap every non-primitive / non-string type with this adapter.
     */
    class Factory : JsonAdapter.Factory {
        private val wrapperTypes = setOf<Class<*>>(
            String::class.java,
            java.lang.String::class.java,
            Boolean::class.java,
            java.lang.Boolean::class.java,
            Int::class.java,
            java.lang.Integer::class.java,
            Long::class.java,
            java.lang.Long::class.java,
            Float::class.java,
            java.lang.Float::class.java,
            Double::class.java,
            java.lang.Double::class.java,
            Byte::class.java,
            java.lang.Byte::class.java,
            Char::class.java,
            java.lang.Character::class.java,
            Short::class.java,
            java.lang.Short::class.java,
        )

        override fun create(
            type: Type,
            annotations: MutableSet<out Annotation>,
            moshi: Moshi
        ): JsonAdapter<*>? {
            val rawType = type.rawType
            if (rawType.isPrimitive || rawType in wrapperTypes) {
                return null
            }
            val delegate = moshi.nextAdapter<Any>(this, type, annotations)
            return FalsyObjectAdapter(delegate)
        }
    }
}

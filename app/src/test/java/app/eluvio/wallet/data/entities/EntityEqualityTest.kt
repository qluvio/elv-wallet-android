package app.eluvio.wallet.data.entities


import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import org.junit.Test
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.Date
import kotlin.random.Random

/**
 * The purpose of this test is to make sure that all Entities have a proper equals implementation.
 * For that, all of their fields must be included, unless they are explicitly excluded
 * in [comparableFields]
 * note: hashcode is not checked - it's assumed it gets updated alongside equals() and even if not,
 * it's not as important.
 */
class EntityEqualityTest {

    @Test
    fun `verify entities have SOME equals() implementation`() {
        /** Classes whose instances are never equal to each other. See [neverEqual] */
        val neverEqual = allEntityClasses.filter { it.neverEqual }

        assert(neverEqual.isEmpty()) {
            """
                >>>Instance never equal (usually missing equals() impl altogether)<<<
                ${neverEqual.joinToString("") { "\t${it.name}\n" }}
            """.trimIndent()
        }
    }

    @Test
    fun `verify entities aren't missing any fields in equals() implementation`() {
        /**
         * Fields that aren't considered in equals() and also not explicitly ignored.
         * See [missedFields], [comparableFields]
         */
        val missedFields = mutableMapOf<Class<*>, List<Field>>()

        allEntityClasses
            .forEach { entityClass ->
                try {
                    // For the sake of cleaner output, ignore classes that are never equal
                    if (!entityClass.neverEqual) {
                        entityClass.missedFields
                            .takeIf { it.isNotEmpty() }
                            ?.let { missedFields[entityClass] = it }
                    }
                } catch (e: Exception) {
                    System.err.println("Unexpected error while processing $entityClass")
                    throw e
                }
            }

        assert(missedFields.isEmpty()) {
            buildString {
                appendLine(">>>Incomplete equals implementation<<<")
                missedFields.forEach { (kls, fields) ->
                    appendLine(kls)
                    appendLine(
                        fields.joinToString("") { "\t${it.type.simpleName} ${it.name}\n" }
                    )
                }
            }
        }
    }

    @Test
    fun `verify @Ignore fields are not included in equals() implementation`() {
        /**
         * [@Ignore] Fields that are considered in equals(), but shouldn't.
         */
        val badFields = mutableMapOf<Class<*>, List<Field>>()

        allEntityClasses.forEach { entityClass ->
            entityClass.badIgnoredFields
                .takeIf { it.isNotEmpty() }
                ?.let { badFields[entityClass] = it }
        }

        assert(badFields.isEmpty()) {
            buildString {
                appendLine(">>> @Ignore fields that are included in equals implementation<<<")
                badFields.forEach { (kls, fields) ->
                    appendLine(kls)
                    appendLine(
                        fields.joinToString("") { "\t${it.type.simpleName} ${it.name}\n" }
                    )
                }
            }
        }
    }

    /**
     * Returns all entities we want to consider.
     */
    private val allEntityClasses: List<Class<out RealmObject>>
        get() = Reflections(
            ConfigurationBuilder()
                .forPackage("app.eluvio.wallet")
                .addUrls(ClasspathHelper.forJavaClassPath())
        )
            .getSubTypesOf(RealmObject::class.java)
            .sortedBy { it.name }

    /**
     * Checks if 2 "blank / fresh" instances of this class aren't equal to each other.
     * Usually this means that there's no equals implementation at all.
     * But this can also happen because we are comparing fields that get a different value upon
     * instance creation (for example random UUID, or Date()).
     */
    private val <T : RealmObject> Class<T>.neverEqual: Boolean
        get() = new() != new()

    /** Convenience method to create a new instance of a class, without using the deprecated [Class.newInstance] */
    private fun <T : Any> Class<T>.new() = getDeclaredConstructor().newInstance()

    /**
     * Returns every [Field], which doesn't break equality checks after mutating.
     */
    private val <T : RealmObject> Class<T>.missedFields: List<Field>
        get() = comparableFields.filterNot { field -> isFieldInEquals(field) }

    /**
     * Returns every @Ignore [Field], which breaks equality checks after mutating.
     */
    private val <T : RealmObject> Class<T>.badIgnoredFields: List<Field>
        get() = ignoredFields.filter { field -> isFieldInEquals(field) }

    /**
     * Checks if mutating [field] breaks equality checks between 2 instances.
     */
    private fun Class<*>.isFieldInEquals(field: Field): Boolean {
        val blankInstance = new()
        val mutatedInstance = new().apply {
            field.makeNotDefault(this)
        }

        return blankInstance != mutatedInstance
    }

    private fun Field.makeNotDefault(instance: Any) {
        this.isAccessible = true
        val newValue = type.nonDefaultValue(this.get(instance))
        this.set(instance, newValue)
    }

    /**
     * Guaranteed to be different than [lastValue].
     * (not really guaranteed, but really really probable)
     */
    private fun <T : Any> Class<T>.nonDefaultValue(lastValue: Any?): Any? {
        return when (typeName) {
            "java.lang.String" -> "str"
            "java.lang.Integer", "int" -> 1
            "java.lang.Long", "long" -> 1L
            "java.lang.Double", "double" -> 1.0
            "java.lang.Float", "float" -> 1f
            "java.lang.Boolean", "boolean" -> lastValue.toString().toBoolean().not()
            "java.util.Date" -> Date(Random.nextLong())
            "java.util.List" -> listOf(1)
            "io.realm.kotlin.types.RealmList" -> {
                // generic types are erased at runtime, so we can make a list of any type we want
                realmListOf<Any>(1).toList().toRealmList()
            }

            "io.realm.kotlin.types.RealmDictionary" -> {
                realmDictionaryOf<Any>("foo" to "bar")
            }

            "io.realm.kotlin.types.RealmInstant" -> {
                RealmInstant.now()
            }
            // Ignored classes
            "android.graphics.drawable.Drawable" -> null
            else -> {
                // non-primitive - attempt to construct random instance recursively
                runCatching { new() }
                    .recoverCatching {
                        // no empty constructor - try to use a constructor with parameters
                        val ctor = constructors.first()
                        val ctorParams = ctor.parameterTypes.map { it.nonDefaultValue(null) }
                        ctor.newInstance(*ctorParams.toTypedArray())
                    }
                    .onSuccess { instance ->
                        comparableFields.forEach { field -> field.makeNotDefault(instance) }
                    }
                    .getOrThrow()
            }
        }
    }

    /**
     * All non-static, non-excluded fields of a class
     */
    private val Class<*>.comparableFields: List<Field>
        get() {
            val excludedFields = listOf(
                // Realm Kotlin SDK adds this field to all classes, just ignore it
                "io_realm_kotlin_objectReference"
            )
            return declaredFields
                .filterNot {
                    Modifier.isStatic(it.modifiers) ||
                            Modifier.isFinal(it.modifiers) ||
                            it.name in excludedFields ||
                            it.isAnnotationPresent(Ignore::class.java)
                }
        }

    /**
     * Only @Ignore fields.
     * Including static/final fields is too much of a hassle, but also even if they are included,
     * they won't break equality checks.
     */
    private val Class<*>.ignoredFields: List<Field>
        get() = declaredFields.filter { it.isAnnotationPresent(Ignore::class.java) }
}

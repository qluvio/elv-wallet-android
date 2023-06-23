package app.eluvio.wallet.data.entities

/**
 * Realm doesn't support Composite primary keys, so we have to manually call updateKey() when we change the fields that make up the primary key.
 */
interface CompositeKeyEntity {
    /**
     * Calculates, updates and returns the primary key
     */
    fun updateKey(): String
}

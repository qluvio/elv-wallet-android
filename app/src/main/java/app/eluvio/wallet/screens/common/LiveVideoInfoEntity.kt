package app.eluvio.wallet.screens.common

import android.content.Context
import android.text.format.DateFormat
import app.eluvio.wallet.data.entities.MediaEntity
import app.eluvio.wallet.util.realm.toDate
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.TypedRealmObject
import io.realm.kotlin.types.annotations.Ignore
import java.util.Locale
import kotlin.reflect.KClass

class LiveVideoInfoEntity : EmbeddedRealmObject {
    /** Copy of wrapping [MediaEntity]'s title, also stored here for convenience */
    var title: String? = null
    var subtitle: String? = null
    var headers: RealmList<String> = realmListOf()

    var startTime: RealmInstant? = null

    @Ignore
    val started: Boolean get() = (startTime ?: RealmInstant.MIN) <= RealmInstant.now()

    var endTime: RealmInstant? = null

    @Ignore
    val ended: Boolean get() = (endTime ?: RealmInstant.MAX) <= RealmInstant.now()

    @Module
    @InstallIn(SingletonComponent::class)
    object EntityModule {
        @Provides
        @IntoSet
        fun provideEntity(): KClass<out TypedRealmObject> = LiveVideoInfoEntity::class
    }

    override fun toString(): String {
        return "LiveVideoInfoEntity(title=$title, subtitle=$subtitle, headers=$headers, startTime=$startTime, endTime=$endTime)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LiveVideoInfoEntity

        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (headers != other.headers) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + headers.hashCode()
        result = 31 * result + (startTime?.hashCode() ?: 0)
        result = 31 * result + (endTime?.hashCode() ?: 0)
        return result
    }
}

fun LiveVideoInfoEntity.getStartDateTimeString(context: Context): String? =
    startTime?.toDate()?.let { startTime ->
        val dateFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), "M/d")
        val date = DateFormat.format(dateFormat, startTime).toString()
        val timeFormat = DateFormat.getTimeFormat(context)
        val time = timeFormat.format(startTime)
        return "$date at $time"
    }

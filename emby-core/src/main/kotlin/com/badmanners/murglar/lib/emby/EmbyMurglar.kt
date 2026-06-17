package com.badmanners.murglar.lib.emby

import com.badmanners.murglar.lib.core.localization.RussianMessages.Companion.RUSSIAN
import com.badmanners.murglar.lib.core.log.LoggerMiddleware
import com.badmanners.murglar.lib.core.model.node.Node
import com.badmanners.murglar.lib.core.model.tag.Tags
import com.badmanners.murglar.lib.core.model.track.source.Bitrate
import com.badmanners.murglar.lib.core.model.track.source.Extension
import com.badmanners.murglar.lib.core.model.track.source.Source
import com.badmanners.murglar.lib.core.network.NetworkMiddleware
import com.badmanners.murglar.lib.core.notification.NotificationMiddleware
import com.badmanners.murglar.lib.core.preference.EditPreference
import com.badmanners.murglar.lib.core.preference.Preference
import com.badmanners.murglar.lib.core.preference.PreferenceMiddleware
import com.badmanners.murglar.lib.core.service.BaseMurglar
import com.badmanners.murglar.lib.emby.api.EmbyApi
import com.badmanners.murglar.lib.emby.localization.EmbyDefaultMessages
import com.badmanners.murglar.lib.emby.localization.EmbyMessages
import com.badmanners.murglar.lib.emby.localization.EmbyRussianMessages
import com.badmanners.murglar.lib.emby.login.EmbyLoginResolver
import com.badmanners.murglar.lib.emby.model.EmbyTrack
import com.badmanners.murglar.lib.emby.node.EmbyNodeResolver
import java.util.Locale.ENGLISH

class EmbyMurglar(
    id: String,
    preferences: PreferenceMiddleware,
    network: NetworkMiddleware,
    notifications: NotificationMiddleware,
    logger: LoggerMiddleware
) : BaseMurglar<EmbyTrack, EmbyMessages>(id, MESSAGES, preferences, network, notifications, logger) {

    companion object {
        private val MESSAGES = mapOf(
            ENGLISH  to EmbyDefaultMessages,
            RUSSIAN  to EmbyRussianMessages
        )
        private const val PREF_SERVER_URL = EmbyLoginResolver.PREF_SERVER_URL
        private const val PREF_TOKEN      = EmbyLoginResolver.PREF_TOKEN
        private const val PREF_USER_ID    = EmbyLoginResolver.PREF_USER_ID
    }

    val api = EmbyApi(this, network)

    override val loginResolver = EmbyLoginResolver(preferences, network, this, messages)

    override val nodeResolver = EmbyNodeResolver(this, messages)

    override val possibleFormats = listOf(Extension.UNKNOWN to Bitrate.B_UNKNOWN)

    val serverUrl: String
        get() = preferences.getString(PREF_SERVER_URL, "").trimEnd('/')

    val token: String
        get() = preferences.getString(PREF_TOKEN, "")

    val userId: String
        get() = preferences.getString(PREF_USER_ID, "")

    override suspend fun onCreate() {
        // token не устаревает
    }

    override val murglarPreferences: List<Preference>
        get() = emptyList()

    override suspend fun resolveSourceForUrl(track: EmbyTrack, source: Source): Source = source

    override suspend fun getTags(track: EmbyTrack, parent: Node?): Tags =
        Tags.Builder().apply {
            title       = track.title
            artists     = track.artistNames
            album       = track.albumName
            trackNumber = track.indexInAlbum
            diskNumber  = track.volumeNumber
            genre       = track.genre
            mediaId     = track.mediaId
        }.createTags()

    override suspend fun getTracksByMediaIds(mediaIds: List<String>): List<EmbyTrack> = emptyList()
}

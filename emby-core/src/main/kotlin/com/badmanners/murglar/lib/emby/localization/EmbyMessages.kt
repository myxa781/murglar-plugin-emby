package com.badmanners.murglar.lib.emby.localization

import com.badmanners.murglar.lib.core.localization.DefaultMessages
import com.badmanners.murglar.lib.core.localization.Messages
import com.badmanners.murglar.lib.core.localization.RussianMessages

interface EmbyMessages : Messages {
    val serverUrlTitle: String
    val serverUrlSummary: String
    val myFavoriteTracks: String
    val myFavoriteAlbums: String
    val myFavoriteArtists: String
    val allTracks: String
    val allAlbums: String
    val allArtists: String
    val itemNotFound: String
    val browse: String
}

object EmbyDefaultMessages : DefaultMessages(), EmbyMessages {
    override val serviceName = "Emby"
    override val serverUrlTitle = "Server URL"
    override val serverUrlSummary = "e.g. http://192.168.1.10:8096"
    override val myFavoriteTracks = "Favorite Tracks"
    override val myFavoriteAlbums = "Favorite Albums"
    override val myFavoriteArtists = "Favorite Artists"
    override val allTracks = "All Tracks"
    override val allAlbums = "All Albums"
    override val allArtists = "All Artists"
    override val itemNotFound = "Item not found on server."
    override val browse = "Browse"
}

object EmbyRussianMessages : RussianMessages(), EmbyMessages {
    override val serviceName = "Emby"
    override val serverUrlTitle = "Адрес сервера"
    override val serverUrlSummary = "например http://192.168.1.10:8096"
    override val myFavoriteTracks = "Избранные треки"
    override val myFavoriteAlbums = "Избранные альбомы"
    override val myFavoriteArtists = "Избранные артисты"
    override val allTracks = "Все треки"
    override val allAlbums = "Все альбомы"
    override val allArtists = "Все артисты"
    override val itemNotFound = "Элемент не найден на сервере."
    override val browse = "Обзор файлов"
}

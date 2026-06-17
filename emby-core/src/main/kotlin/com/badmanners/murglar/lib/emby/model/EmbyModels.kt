package com.badmanners.murglar.lib.emby.model

import com.badmanners.murglar.lib.core.model.album.BaseAlbum
import com.badmanners.murglar.lib.core.model.artist.BaseArtist
import com.badmanners.murglar.lib.core.model.node.BaseNode
import com.badmanners.murglar.lib.core.model.node.NodeType
import com.badmanners.murglar.lib.core.model.playlist.BasePlaylist
import com.badmanners.murglar.lib.core.model.track.BaseTrack
import com.badmanners.murglar.lib.core.model.track.source.Source
import com.badmanners.murglar.lib.core.utils.contract.Model
import org.threeten.bp.LocalDate

@Model
class EmbyTrack(
    id: String,
    title: String,
    subtitle: String?,
    override val artistIds: List<String>,
    override val artistNames: List<String>,
    override val albumId: String?,
    override val albumName: String?,
    albumReleaseDate: LocalDate?,
    indexInAlbum: Int?,
    volumeNumber: Int?,
    override val durationMs: Long,
    override val genre: String?,
    explicit: Boolean,
    sources: List<Source>,
    override val nodeType: String,
    mediaId: String,
    smallCoverUrl: String?,
    bigCoverUrl: String?,
    serviceUrl: String?
) : BaseTrack(
    id = id,
    title = title,
    subtitle = subtitle,
    artistIds = artistIds,
    artistNames = artistNames,
    albumId = albumId,
    albumName = albumName,
    albumReleaseDate = albumReleaseDate,
    indexInAlbum = indexInAlbum,
    volumeNumber = volumeNumber,
    durationMs = durationMs,
    genre = genre,
    explicit = explicit,
    gain = null,
    peak = null,
    sources = sources,
    mediaId = mediaId,
    smallCoverUrl = smallCoverUrl,
    bigCoverUrl = bigCoverUrl,
    serviceUrl = serviceUrl
)

@Model
class EmbyAlbum(
    id: String,
    title: String,
    description: String?,
    releaseDate: LocalDate?,
    smallCoverUrl: String?,
    bigCoverUrl: String?,
    tracksCount: Int,
    artistIds: List<String>,
    artistNames: List<String>,
    genre: String?,
    override val nodeType: String,
    explicit: Boolean,
    serviceUrl: String?
) : BaseAlbum(
    id = id,
    title = title,
    description = description,
    artistIds = artistIds,
    artistNames = artistNames,
    tracksCount = tracksCount,
    releaseDate = releaseDate,
    genre = genre,
    explicit = explicit,
    smallCoverUrl = smallCoverUrl,
    bigCoverUrl = bigCoverUrl,
    serviceUrl = serviceUrl
)

@Model
class EmbyArtist(
    id: String,
    name: String,
    smallCoverUrl: String?,
    bigCoverUrl: String?,
    serviceUrl: String?
) : BaseArtist(
    id = id,
    name = name,
    smallCoverUrl = smallCoverUrl,
    bigCoverUrl = bigCoverUrl,
    serviceUrl = serviceUrl
)

@Model
class EmbyPlaylist(
    id: String,
    title: String,
    tracksCount: Int,
    smallCoverUrl: String?,
    bigCoverUrl: String?,
    serviceUrl: String?
) : BasePlaylist(
    id = id,
    title = title,
    tracksCount = tracksCount,
    smallCoverUrl = smallCoverUrl,
    bigCoverUrl = bigCoverUrl,
    serviceUrl = serviceUrl
)

@Model
class EmbyFolder(
    id: String,
    name: String,
    smallCoverUrl: String?,
    bigCoverUrl: String?,
    serviceUrl: String?
) : BaseNode(
    nodeId = id,
    nodeName = name,
    smallCoverUrl = smallCoverUrl,
    bigCoverUrl = bigCoverUrl,
    serviceUrl = serviceUrl
)

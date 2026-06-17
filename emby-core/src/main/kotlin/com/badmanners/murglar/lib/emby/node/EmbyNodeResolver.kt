package com.badmanners.murglar.lib.emby.node

import com.badmanners.murglar.lib.core.model.node.NamedPath
import com.badmanners.murglar.lib.core.model.node.Node
import com.badmanners.murglar.lib.core.model.node.Node.Companion.to
import com.badmanners.murglar.lib.core.model.node.NodeParameters.PagingType.NON_PAGEABLE
import com.badmanners.murglar.lib.core.model.node.NodeParameters.PagingType.PAGEABLE
import com.badmanners.murglar.lib.core.model.node.NodeType.ALBUM
import com.badmanners.murglar.lib.core.model.node.NodeType.ARTIST
import com.badmanners.murglar.lib.core.model.node.NodeType.NODE
import com.badmanners.murglar.lib.core.model.node.NodeType.TRACK
import com.badmanners.murglar.lib.core.model.node.Path
import com.badmanners.murglar.lib.core.node.BaseNodeResolver
import com.badmanners.murglar.lib.core.node.Directory
import com.badmanners.murglar.lib.core.node.LikeConfig
import com.badmanners.murglar.lib.core.node.MappedEntity
import com.badmanners.murglar.lib.core.node.Root
import com.badmanners.murglar.lib.core.node.Search
import com.badmanners.murglar.lib.core.node.Track
import com.badmanners.murglar.lib.emby.EmbyMurglar
import com.badmanners.murglar.lib.emby.localization.EmbyMessages
import com.badmanners.murglar.lib.emby.model.EmbyAlbum
import com.badmanners.murglar.lib.emby.model.EmbyArtist
import com.badmanners.murglar.lib.emby.model.EmbyFolder
import com.badmanners.murglar.lib.emby.model.EmbyTrack
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class EmbyNodeResolver(
    murglar: EmbyMurglar,
    messages: EmbyMessages
) : BaseNodeResolver<EmbyMurglar, EmbyMessages>(murglar, messages) {

    override val configurations = listOf(

        Root(pattern = "favoriteTracks",  name = messages::myFavoriteTracks,  paging = PAGEABLE, hasSubdirectories = false, isOwn = true,  contentNodeType = TRACK,  nodeContentSupplier = ::getFavoriteTracks),
        Root(pattern = "favoriteAlbums",  name = messages::myFavoriteAlbums,  paging = PAGEABLE, hasSubdirectories = true,  isOwn = true,  contentNodeType = ALBUM,  nodeContentSupplier = ::getFavoriteAlbums),
        Root(pattern = "favoriteArtists", name = messages::myFavoriteArtists, paging = PAGEABLE, hasSubdirectories = true,  isOwn = true,  contentNodeType = ARTIST, nodeContentSupplier = ::getFavoriteArtists),
        Root(pattern = "allTracks",       name = messages::allTracks,         paging = PAGEABLE, hasSubdirectories = false, isOwn = false, contentNodeType = TRACK,  nodeContentSupplier = ::getAllTracks),
        Root(pattern = "allAlbums",       name = messages::allAlbums,         paging = PAGEABLE, hasSubdirectories = true,  isOwn = false, contentNodeType = ALBUM,  nodeContentSupplier = ::getAllAlbums),
        Root(pattern = "allArtists",      name = messages::allArtists,        paging = PAGEABLE, hasSubdirectories = true,  isOwn = false, contentNodeType = ARTIST, nodeContentSupplier = ::getAllArtists),

        Search(pattern = "searchTracks",  name = messages::tracksSearch,  hasSubdirectories = false, contentNodeType = TRACK,  nodeContentSupplier = ::searchTracks),
        Search(pattern = "searchAlbums",  name = messages::albumsSearch,  hasSubdirectories = true,  contentNodeType = ALBUM,  nodeContentSupplier = ::searchAlbums),
        Search(pattern = "searchArtists", name = messages::artistsSearch, hasSubdirectories = true,  contentNodeType = ARTIST, nodeContentSupplier = ::searchArtists),

        Track(
            pattern = "*/track-<trackId>",
            like = LikeConfig(rootNodePath("favoriteTracks"), ::likeTrack),
            relatedPaths = ::getTrackRelatedPaths,
            nodeSupplier = ::getTrack
        ),

        MappedEntity(
            pattern = "*/album-<albumId>",
            paging = NON_PAGEABLE, hasSubdirectories = false, type = ALBUM, contentNodeType = TRACK,
            relatedPaths = { emptyList() },
            like = LikeConfig(rootNodePath("favoriteAlbums"), ::likeAlbum),
            nodeSupplier = ::getAlbum,
            nodeContentSupplier = ::getAlbumTracks
        ),

        MappedEntity(
            pattern = "*/artist-<artistId>",
            paging = NON_PAGEABLE, hasSubdirectories = true, type = ARTIST, contentNodeType = NODE,
            relatedPaths = { emptyList() },
            like = LikeConfig(rootNodePath("favoriteArtists"), ::likeArtist),
            nodeSupplier = ::getArtist,
            nodeContentSupplier = ::getArtistSubdirectories
        ),
        Directory(
            pattern = "*/artist-<artistId>/albums",
            paging = PAGEABLE, hasSubdirectories = true, contentNodeType = ALBUM,
            nodeContentSupplier = ::getArtistAlbums
        ),

        // --- Обзор файловой системы ---
        Root(
            pattern = "browse",
            name = messages::browse,
            paging = NON_PAGEABLE,
            hasSubdirectories = true,
            isOwn = false,
            contentNodeType = NODE,
            nodeContentSupplier = ::getBrowseRoot
        ),
        Directory(
            pattern = "*/folder-<folderId>",
            paging = NON_PAGEABLE,
            hasSubdirectories = true,
            contentNodeType = NODE,
            nodeContentSupplier = ::getFolderChildren
        )
    )

    // Roots
    private suspend fun getFavoriteTracks(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("Audio",       isFavorite = true, page = page).tracks(p)
    private suspend fun getFavoriteAlbums(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("MusicAlbum",  isFavorite = true, page = page).albums(p)
    private suspend fun getFavoriteArtists(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("MusicArtist", isFavorite = true, page = page).artists(p)
    private suspend fun getAllTracks(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("Audio",       page = page).tracks(p)
    private suspend fun getAllAlbums(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("MusicAlbum",  page = page).albums(p)
    private suspend fun getAllArtists(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("MusicArtist", page = page).artists(p)

    // Search
    private suspend fun searchTracks(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("Audio",       searchTerm = params.getQuery(), page = page ?: 0).tracks(p)
    private suspend fun searchAlbums(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("MusicAlbum",  searchTerm = params.getQuery(), page = page ?: 0).albums(p)
    private suspend fun searchArtists(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("MusicArtist", searchTerm = params.getQuery(), page = page ?: 0).artists(p)

    // Track
    private suspend fun getTrack(p: Path, params: Map<String, String>) =
        with(murglar.api) { getItem(params["trackId"]!!).toTrack()!! }.convertTrack(p)

    private fun getTrackRelatedPaths(node: Node): List<NamedPath> {
        val t = node.to<EmbyTrack>()
        return buildList {
            if (!t.albumId.isNullOrEmpty())
                add(NamedPath(t.albumName ?: t.albumId!!, ALBUM, unmappedPath().child("album-${t.albumId}")))
            t.artistNames.forEachIndexed { i, name ->
                if (i < t.artistIds.size)
                    add(NamedPath(name, ARTIST, unmappedPath().child("artist-${t.artistIds[i]}")))
            }
        }
    }

    // Album
    private suspend fun getAlbum(p: Path, params: Map<String, String>) =
        with(murglar.api) { getItem(params["albumId"]!!).toAlbum()!! }.convertAlbum(p)
    private suspend fun getAlbumTracks(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("Audio", parentId = params["albumId"]!!, sortBy = "IndexNumber").tracks(p)

    // Artist
    private suspend fun getArtist(p: Path, params: Map<String, String>) =
        with(murglar.api) { getItem(params["artistId"]!!).toArtist()!! }.convertArtist(p)
    private fun getArtistSubdirectories(p: Path, page: Int?, params: Map<String, String>) = listOf(
        subdirectoryNode("albums", messages.albums, p)
    )
    private suspend fun getArtistAlbums(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getItems("MusicAlbum", parentId = params["artistId"]!!, page = page).albums(p)

    // Likes — suspend т.к. addFavorite/removeFavorite suspend
    private suspend fun likeTrack(node: Node, like: Boolean) {
        val id = node.to<EmbyTrack>().id
        if (like) murglar.api.addFavorite(id) else murglar.api.removeFavorite(id)
    }
    private suspend fun likeAlbum(node: Node, like: Boolean) {
        val id = node.to<EmbyAlbum>().id
        if (like) murglar.api.addFavorite(id) else murglar.api.removeFavorite(id)
    }
    private suspend fun likeArtist(node: Node, like: Boolean) {
        val id = node.to<EmbyArtist>().id
        if (like) murglar.api.addFavorite(id) else murglar.api.removeFavorite(id)
    }

    // Browse
    private suspend fun getBrowseRoot(p: Path, page: Int?, params: Map<String, String>) =
        murglar.api.getMusicLibraries().map { it.convertFolder(p) }

    private suspend fun getFolder(p: Path, params: Map<String, String>): com.badmanners.murglar.lib.core.model.node.Node {
        val folderId = params["folderId"]!!
        // Не вызываем getItem — он падает для папок Emby
        // Имя папки берём из уже загруженного контента родителя
        // Создаём минимальную ноду с id
        val folder = EmbyFolder(folderId, folderId, null, null, murglar.api.webUrl(folderId))
        return folder.convertFolder(p)
    }

    private suspend fun getFolderChildren(p: Path, page: Int?, params: Map<String, String>): List<com.badmanners.murglar.lib.core.model.node.Node> {
        val result = murglar.api.getFolderChildren(params["folderId"]!!)
        val items = result["Items"]?.let {
            it as? kotlinx.serialization.json.JsonArray
        } ?: return emptyList()

        return items.mapNotNull { el ->
            val obj = el.jsonObject
            val type = obj["Type"]?.jsonPrimitive?.content ?: return@mapNotNull null
            when (type) {
                "Audio" -> with(murglar.api) { obj.toTrack() }?.convertTrack(p)
                "MusicAlbum" -> with(murglar.api) { obj.toAlbum() }?.convertAlbum(p)
                "MusicArtist" -> with(murglar.api) { obj.toArtist() }?.convertArtist(p)
                else -> with(murglar.api) { obj.toFolder() }?.convertFolder(p)
            }
        }
    }

        override suspend fun getTracksByMediaIds(mediaIds: List<String>): List<EmbyTrack> = emptyList()


    private fun folderPath(p: Path, t: EmbyFolder): Path = p.child("folder-" + t.nodeId)
    private fun EmbyFolder.convertFolder(p: Path) = convert(::folderPath, p)

    private fun trackPath(p: Path, t: EmbyTrack): Path   = p.child("track-${t.id}")
    private fun albumPath(p: Path, t: EmbyAlbum): Path   = p.child("album-${t.id}")
    private fun artistPath(p: Path, t: EmbyArtist): Path = p.child("artist-${t.id}")

    private fun EmbyTrack.convertTrack(p: Path)   = convert(::trackPath, p)
    private fun EmbyAlbum.convertAlbum(p: Path)   = convert(::albumPath, p)
    private fun EmbyArtist.convertArtist(p: Path) = convert(::artistPath, p)

    private fun JsonObject.tracks(p: Path)  = murglar.api.run { itemsToTracks()  }.map { it.convertTrack(p)  }
    private fun JsonObject.albums(p: Path)  = murglar.api.run { itemsToAlbums()  }.map { it.convertAlbum(p)  }
    private fun JsonObject.artists(p: Path) = murglar.api.run { itemsToArtists() }.map { it.convertArtist(p) }
}

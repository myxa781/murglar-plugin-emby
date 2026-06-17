package com.badmanners.murglar.lib.emby.api

import com.badmanners.murglar.lib.core.network.NetworkMiddleware
import com.badmanners.murglar.lib.core.network.NetworkRequest
import com.badmanners.murglar.lib.core.network.ResponseConverters
import com.badmanners.murglar.lib.core.utils.getJsonArray
import com.badmanners.murglar.lib.core.utils.getJsonObject
import com.badmanners.murglar.lib.core.utils.getString
import com.badmanners.murglar.lib.core.utils.getStringOpt
import com.badmanners.murglar.lib.core.utils.getIntOpt
import com.badmanners.murglar.lib.core.utils.has
import com.badmanners.murglar.lib.core.utils.string
import com.badmanners.murglar.lib.emby.EmbyMurglar
import com.badmanners.murglar.lib.emby.model.EmbyAlbum
import com.badmanners.murglar.lib.emby.model.EmbyArtist
import com.badmanners.murglar.lib.emby.model.EmbyFolder
import com.badmanners.murglar.lib.emby.model.EmbyPlaylist
import com.badmanners.murglar.lib.emby.model.EmbyTrack
import com.badmanners.murglar.lib.core.model.node.NodeType
import com.badmanners.murglar.lib.core.model.track.source.Bitrate
import com.badmanners.murglar.lib.core.model.track.source.Container
import com.badmanners.murglar.lib.core.model.track.source.Extension
import com.badmanners.murglar.lib.core.model.track.source.Source
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.threeten.bp.LocalDate

class EmbyApi(private val murglar: EmbyMurglar, private val network: NetworkMiddleware) {

    private val serverUrl get() = murglar.serverUrl
    private val token     get() = murglar.token
    private val userId    get() = murglar.userId

    fun authHeader() =
        """MediaBrowser Client="Murglar", Device="Murglar", DeviceId="murglar-emby", Version="1.0.0", Token="$token""""

    fun coverUrl(itemId: String) = "$serverUrl/Items/$itemId/Images/Primary?api_key=$token"
    fun streamUrl(itemId: String) = "$serverUrl/Audio/$itemId/stream?static=true&api_key=$token"
    fun webUrl(itemId: String) = "$serverUrl/web/index.html#!/itemdetails.html?id=$itemId"

    // -------------------------------------------------------------------------
    // HTTP — все методы suspend, т.к. network.execute suspend
    // -------------------------------------------------------------------------

    private suspend fun get(path: String, vararg params: Pair<String, Any?>): JsonObject {
        val builder = NetworkRequest.Builder("$serverUrl$path", "GET")
            .addHeader("X-Emby-Token", token)
            .addHeader("X-MediaBrowser-Token", token)
        params.forEach { (k, v) -> if (v != null) builder.addParameter(k, v.toString()) }
        return network.execute(builder.build(), ResponseConverters.asJsonObject()).result
    }

    private suspend fun getArr(path: String, vararg params: Pair<String, Any?>): JsonArray {
        val builder = NetworkRequest.Builder("$serverUrl$path", "GET")
            .addHeader("X-Emby-Token", token)
            .addHeader("X-MediaBrowser-Token", token)
        params.forEach { (k, v) -> if (v != null) builder.addParameter(k, v.toString()) }
        return network.execute(builder.build(), ResponseConverters.asJsonArray()).result
    }

    private suspend fun post(path: String, header: String, body: String): JsonObject {
        val builder = NetworkRequest.Builder("$serverUrl$path", "POST")
            .addHeader("Authorization", header)
            .body(body)
        return network.execute(builder.build(), ResponseConverters.asJsonObject()).result
    }

    private suspend fun del(path: String) {
        val builder = NetworkRequest.Builder("$serverUrl$path", "DELETE")
            .addHeader("Authorization", authHeader())
        network.execute(builder.build(), ResponseConverters.asString())
    }

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    suspend fun authenticateByName(username: String, password: String): Triple<String, String, String> {
        val noToken = """MediaBrowser Client="Murglar", Device="Murglar", DeviceId="murglar-emby", Version="1.0.0""""
        val body = """{"Username":"${username.esc()}","Pw":"${password.esc()}"}"""
        val result = post("/Users/AuthenticateByName", noToken, body)
        val accessToken = result.getString("AccessToken")
        val user = result.getJsonObject("User")
        val uid = user.getString("Id")
        val name = user.getStringOpt("Name") ?: username
        return Triple(accessToken, uid, name)
    }

    suspend fun getUserId(apiKey: String): String {
        val header = """MediaBrowser Client="Murglar", Device="Murglar", DeviceId="murglar-emby", Version="1.0.0", Token="$apiKey""""
        val builder = NetworkRequest.Builder("$serverUrl/Users/Me", "GET")
            .addHeader("Authorization", header)
        return network.execute(builder.build(), ResponseConverters.asJsonObject()).result.getString("Id")
    }

    // -------------------------------------------------------------------------
    // Items
    // -------------------------------------------------------------------------

    suspend fun getItems(
        includeTypes: String,
        parentId: String? = null,
        isFavorite: Boolean? = null,
        searchTerm: String? = null,
        ids: String? = null,
        sortBy: String = "SortName",
        page: Int? = null,
        pageSize: Int = 100
    ): JsonObject = get(
        "/Items",
        "userId" to userId,
        "IncludeItemTypes" to includeTypes,
        "Recursive" to "true",
        "SortBy" to sortBy,
        "SortOrder" to "Ascending",
        "Fields" to "MediaSources,MediaStreams,Genres,ArtistItems,Container,Bitrate",
        "ParentId" to parentId,
        "IsFavorite" to isFavorite,
        "SearchTerm" to searchTerm,
        "Ids" to ids,
        "StartIndex" to page?.let { it * pageSize },
        "Limit" to page?.let { pageSize }
    )

    suspend fun getItem(itemId: String): JsonObject =
        get("/Users/$userId/Items/$itemId", "Fields" to "MediaSources,MediaStreams,Genres,ArtistItems,Container,Bitrate")

    /**
     * Получить дочерние элементы папки (не рекурсивно).
     * Использует отдельный запрос без Recursive=true.
     */
    suspend fun getFolderChildren(folderId: String): JsonObject =
        get("/Items",
            "userId" to userId,
            "ParentId" to folderId,
            "SortBy" to "SortName",
            "SortOrder" to "Ascending"
        )

    fun JsonObject.toFolder(): EmbyFolder? {
        val itemId = this["Id"]?.jsonPrimitive?.content ?: return null
        val name   = this["Name"]?.jsonPrimitive?.content ?: return null
        val cover  = if (has("ImageTags")) coverUrl(itemId) else null
        return EmbyFolder(itemId, name, cover, cover, webUrl(itemId))
    }

    fun JsonObject.itemsToFolders() =
        try { getJsonArray("Items").mapNotNull { it.jsonObject.toFolder() } } catch (_: Exception) { emptyList() }

        suspend fun getMusicLibraries(): List<EmbyFolder> {
        val arr = getArr("/Library/VirtualFolders", "api_key" to token)
        return arr.mapNotNull { el ->
            val obj = el.jsonObject
            if (obj["CollectionType"]?.jsonPrimitive?.content != "music") return@mapNotNull null
            val itemId = obj["ItemId"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val name = obj["Name"]?.jsonPrimitive?.content ?: itemId
            EmbyFolder(itemId, name, null, null, webUrl(itemId))
        }
    }

    suspend fun addFavorite(itemId: String) {
        post("/Users/$userId/FavoriteItems/$itemId", authHeader(), "")
    }

    suspend fun removeFavorite(itemId: String) = del("/Users/$userId/FavoriteItems/$itemId")

    // -------------------------------------------------------------------------
    // Converters
    // -------------------------------------------------------------------------

    fun JsonObject.toTrack(): EmbyTrack? {
        val itemId = this["Id"]?.jsonPrimitive?.content ?: return null
        val name   = this["Name"]?.jsonPrimitive?.content ?: return null

        val albumId   = this["AlbumId"]?.jsonPrimitive?.content
        val albumName = this["Album"]?.jsonPrimitive?.content
        val artistNames = try { getJsonArray("Artists").map { it.string } }
                          catch (_: Exception) { listOfNotNull(getStringOpt("AlbumArtist")) }
        val artistIds   = try { getJsonArray("ArtistItems").map { it.jsonObject.getString("Id") } }
                          catch (_: Exception) { emptyList() }

        val indexInAlbum = getIntOpt("IndexNumber")
        val volumeNumber = getIntOpt("ParentIndexNumber")
        val durationMs   = (this["RunTimeTicks"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L) / 10_000L
        val genre        = try { getJsonArray("Genres").firstOrNull()?.string } catch (_: Exception) { null }
        val year         = getIntOpt("ProductionYear")
        val releaseDate  = year?.let { runCatching { LocalDate.of(it, 1, 1) }.getOrNull() }

        val mediaSource = try { getJsonArray("MediaSources").firstOrNull()?.jsonObject } catch (_: Exception) { null }

        // Контейнер — из MediaSources или прямо из поля Container
        val container = mediaSource?.getStringOpt("Container")
            ?: this["Container"]?.jsonPrimitive?.content
            ?: "unknown"

        // Битрейт — из MediaSources, или из MediaStreams (аудио поток), или из прямого поля
        val bitrate = mediaSource?.getIntOpt("Bitrate")
            ?: try {
                getJsonArray("MediaStreams")
                    .firstOrNull { it.jsonObject["Type"]?.jsonPrimitive?.content == "Audio" }
                    ?.jsonObject?.getIntOpt("BitRate")
            } catch (_: Exception) { null }
            ?: this["Bitrate"]?.jsonPrimitive?.content?.toIntOrNull()

        val size = mediaSource?.get("Size")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

        val cover = if (has("ImageTags")) coverUrl(itemId) else null

        val ext = when (container.lowercase()) {
            "mp3" -> Extension.MP3; "flac" -> Extension.FLAC
            "aac", "m4a" -> Extension.AAC; "ogg", "oga" -> Extension.OGG
            "wav" -> Extension.WAV; "wma" -> Extension.WMA
            else -> Extension.UNKNOWN
        }
        val lossless = ext == Extension.FLAC || ext == Extension.WAV
        val br = when {
            lossless -> Bitrate.B_HI_RES
            bitrate == null -> Bitrate.B_UNKNOWN
            bitrate >= 320_000 -> Bitrate.B_320
            bitrate >= 256_000 -> Bitrate.B_256
            bitrate >= 192_000 -> Bitrate.B_192
            bitrate >= 128_000 -> Bitrate.B_128
            else -> Bitrate.B_UNKNOWN
        }

        // Source(id, url, tag, extension, container, bitrate, size)
        val source = Source(itemId, streamUrl(itemId), container, ext, Container.PROGRESSIVE, br, size)

        return EmbyTrack(
            id = itemId, title = name, subtitle = null,
            artistIds = artistIds, artistNames = artistNames.ifEmpty { listOf("") },
            albumId = albumId, albumName = albumName, albumReleaseDate = releaseDate,
            indexInAlbum = indexInAlbum, volumeNumber = volumeNumber, durationMs = durationMs,
            genre = genre, explicit = false,
            sources = listOf(source),
            nodeType = NodeType.TRACK, mediaId = itemId,
            smallCoverUrl = cover, bigCoverUrl = cover, serviceUrl = webUrl(itemId)
        )
    }

    fun JsonObject.toAlbum(): EmbyAlbum? {
        val itemId = this["Id"]?.jsonPrimitive?.content ?: return null
        val name   = this["Name"]?.jsonPrimitive?.content ?: return null
        val artistNames = try { getJsonArray("ArtistItems").map { it.jsonObject.getString("Name") } } catch (_: Exception) { emptyList() }
        val artistIds   = try { getJsonArray("ArtistItems").map { it.jsonObject.getString("Id") }   } catch (_: Exception) { emptyList() }
        val tracksCount = getIntOpt("ChildCount") ?: 0
        val year = getIntOpt("ProductionYear")
        val releaseDate = year?.let { runCatching { LocalDate.of(it, 1, 1) }.getOrNull() }
        val cover = if (has("ImageTags")) coverUrl(itemId) else null
        return EmbyAlbum(itemId, name, null, releaseDate, cover, cover, tracksCount, artistIds, artistNames, null, NodeType.ALBUM, false, webUrl(itemId))
    }

    fun JsonObject.toArtist(): EmbyArtist? {
        val itemId = this["Id"]?.jsonPrimitive?.content ?: return null
        val name   = this["Name"]?.jsonPrimitive?.content ?: return null
        val cover  = if (has("ImageTags")) coverUrl(itemId) else null
        return EmbyArtist(itemId, name, cover, cover, webUrl(itemId))
    }

    fun JsonObject.toPlaylist(): EmbyPlaylist? {
        val itemId = this["Id"]?.jsonPrimitive?.content ?: return null
        val name   = this["Name"]?.jsonPrimitive?.content ?: return null
        val count  = getIntOpt("ChildCount") ?: 0
        val cover  = if (has("ImageTags")) coverUrl(itemId) else null
        return EmbyPlaylist(itemId, name, count, cover, cover, webUrl(itemId))
    }

    fun JsonObject.itemsToTracks()    = try { getJsonArray("Items").mapNotNull { it.jsonObject.toTrack() }    } catch (_: Exception) { emptyList() }
    fun JsonObject.itemsToAlbums()    = try { getJsonArray("Items").mapNotNull { it.jsonObject.toAlbum() }    } catch (_: Exception) { emptyList() }
    fun JsonObject.itemsToArtists()   = try { getJsonArray("Items").mapNotNull { it.jsonObject.toArtist() }   } catch (_: Exception) { emptyList() }
    fun JsonObject.itemsToPlaylists() = try { getJsonArray("Items").mapNotNull { it.jsonObject.toPlaylist() } } catch (_: Exception) { emptyList() }

    private fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
}

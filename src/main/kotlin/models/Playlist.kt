package models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Playlists : LongIdTable() {
    val user = varchar("UserId", 64)
    val guild = varchar("GuildId", 64)
    val name = varchar("Name", 200)
}

object Songs : LongIdTable() {
    val playlist = reference("Playlist", Playlists)
    val name = varchar("Name", 200)
    val url = varchar("Url", 2000)
}

class Playlist(id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<Playlist>(Playlists)

    var user by Playlists.user
    var guild by Playlists.guild
    var name by Playlists.name
    val songs by Song referrersOn Songs.playlist
}

class Song(id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<Song>(Songs)

    var playlist by Playlist referencedOn Songs.playlist
    var name by Songs.name
    var url by Songs.url
}

data class JsonPlaylist @JsonCreator constructor(
        @JsonProperty("id") val id : Long,
        @JsonProperty("user") val user: String,
        @JsonProperty("guild") val guild: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("songs") val songs : List<JsonSong>)

data class JsonSong @JsonCreator constructor(
        @JsonProperty("id") val id: Long,
        @JsonProperty("name") val name: String,
        @JsonProperty("url")  val url: String)

val Playlist.json : JsonPlaylist get() {
    return transaction { JsonPlaylist(id.value, user, guild, name, songs.map { it.json }) }
}

val Song.json : JsonSong get() {
    return JsonSong(id.value, name, url)
}
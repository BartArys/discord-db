package controllers

import io.javalin.Context
import models.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import parameterRequired
import sendNotFound

object PlaylistController{
    private val logger = LoggerFactory.getLogger(this::class.java)

    val id = "id"
    val user = "user"
    val guild = "guild"
    val name = "name"

    /*
    private val connectedSessions : MutableList<WsSession> = mutableListOf()

    fun onWsConnect(session: WsSession){
        connectedSessions.add(session)
        logger.info("new session: ${session.id}")
    }

    fun onWsDisconnect(session: WsSession, status: Int, message: String){
        connectedSessions.remove(session)
        logger.info("disconnected session: ${session.id}, status: $status, message: $message")
    }

    private fun notifyPrefixChange(json: JsonPlaylist){
        connectedSessions.forEach { it.send(JavalinJacksonPlugin.toJson(json)) }
    }
    */

    fun getAllPlaylists(context: Context){
        transaction { Playlist.all().map { it.json }.let { context.json(it) } }
    }

    fun getPlaylistById(context: Context){
        context.param(id)?.let {
            transaction {
                Playlist.findById(it.toLong())?.json?.let { context.json(it) }
                        ?: context.sendNotFound()
            }
        } ?: context.parameterRequired(id)
    }

    fun getPlaylistsByUser(context: Context){
        context.param(user)?.let {
            transaction {
                Playlist.find { (Playlists.user eq it) }.map { it.json }.let { context.json(it) }
            }
        } ?: context.parameterRequired(user)
    }

    fun getPlaylistsByGuild(context: Context){
        context.param(guild)?.let {
            transaction {
                Playlist.find { (Playlists.guild eq it) }.map { it.json }.let { context.json(it) }
            }
        } ?: context.parameterRequired(guild)
    }

    fun getPlaylistsByName(context: Context){
        context.param(name)?.let {
            transaction {
                Playlist.find { (Playlists.name eq it) }.map { it.json }.let { context.json(it) }
            }
        } ?: context.parameterRequired(name)
    }

    fun addSongToPlaylist(context: Context){
        context.bodyAsClass(JsonSong::class.java).let { json ->
            transaction {
                val playlist = Playlist.findById(context.param(id)!!.toLong())!!

                val song = Song.new {
                    name = json.name
                    url = json.url
                    this.playlist = playlist
                }
                context.json(song.json)
            }
        }
    }

    fun addPlaylist(context: Context){
        context.bodyAsClass(JsonPlaylist::class.java).let { json ->
            transaction {
                val playlist = Playlist.new {
                    name = json.name
                    guild = json.guild
                    name = json.name
                    user = json.user
                }

                json.songs.forEach {
                    Song.new {
                        this.playlist = playlist
                        name = it.name
                        url = it.url
                    }
                }

                playlist.let { context.json(it.json) }
            }
        }
    }

    fun deleteSong(context: Context){
        context.param(id)?.let {
            transaction {
                Song.findById(it.toLong())?.delete()?.let {
                    context.status(202).result("ok")
                }?: context.sendNotFound()
            }
        } ?: context.parameterRequired(id)
    }

    fun deletePlaylist(context: Context){
        context.param(id)?.let {
            transaction {
                Playlist.findById(it.toLong())?.delete()?.let {
                    context.status(202).result("ok")
                }?: context.sendNotFound()
            }
        } ?: context.parameterRequired(id)
    }

    fun updatePlaylist(context: Context){
        context.bodyAsClass(JsonPlaylist::class.java).let { json ->
            transaction {
                Playlists.update( { Playlists.id eq json.id } ){
                    it[user] = json.user
                    it[guild] = json.guild
                    it[name] = json.name
                }
            }
            context.json(json)
        }
    }

    fun updateSong(context: Context){
        context.bodyAsClass(JsonSong::class.java).let { json ->
            transaction {
                Songs.update( { Playlists.id eq json.id } ){
                    it[name] = json.name
                    it[url] = json.url
                }
                context.json(json)
            }
        }
    }


}
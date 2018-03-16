
import controllers.PlaylistController
import controllers.ReactionController
import controllers.UserActivityController
import controllers.UserPrefixController
import io.javalin.ApiBuilder.*
import io.javalin.Context
import io.javalin.Javalin
import models.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.Slf4jSqlLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>){
    Database.connect("jdbc:h2:file:D:/discordbot/postgres/reactions", driver = "org.h2.Driver")

    transaction {
        logger.addLogger(Slf4jSqlLogger)
        create(UserActivities, Reactions, UserPrefixes, Songs, Playlists)
    }

    val app = Javalin.create().port(6969)

    app.ws("/prefixes") {
        it.onConnect(UserPrefixController::onWsConnect)
        it.onClose(UserPrefixController::onWsDisconnect)
        it.onMessage(UserPrefixController::onWsMessage)
    }

    app.routes {
        prefixRoutes()
        playlistRoutes()
        activityRoutes()
        reactionRoutes()
    }

    app.start()
}

fun prefixRoutes(){
    path("prefixes") {
        get(UserPrefixController::getPrefixes)
        post(UserPrefixController::addPrefix)
        patch(UserPrefixController::updatePrefixForUser)

        path("/user/:${UserPrefixController.user}"){
            get(UserPrefixController::getPrefixForUser)
            delete(UserPrefixController::removePrefixForUser)
        }
    }
}

fun playlistRoutes() {
    path("playlists"){
        get(PlaylistController::getAllPlaylists)
        post(PlaylistController::addPlaylist)
        patch(PlaylistController::updatePlaylist)
        delete(PlaylistController::deletePlaylist)

        path("/id/:${PlaylistController.id}"){
            get(PlaylistController::getPlaylistById)
        }

        path("/user/:${PlaylistController.user}"){
            get(PlaylistController::getPlaylistsByUser)
        }

        path("/name/:${PlaylistController.name}"){
            get(PlaylistController::getPlaylistsByName)
        }

        path("/guild/:${PlaylistController.guild}"){
            get(PlaylistController::getPlaylistsByGuild)
        }
        path("/songs/:${PlaylistController.id}"){
            delete(PlaylistController::deleteSong)
        }

        path("/:${PlaylistController.id}") {
            post(PlaylistController::addSongToPlaylist)
            patch(PlaylistController::updateSong)
        }
    }
}

fun activityRoutes(){
    path("activities") {
        post(UserActivityController::addUserActivity)
        delete(UserActivityController::removeUserActivity)
        patch(UserActivityController::updateUserActivity)

        path("/user/:${UserActivityController.user}"){
            get(UserActivityController::getActivitiesByUser)
        }
    }
}

fun reactionRoutes(){
    path("reactions"){
        get(ReactionController::getAllReactions)
        post(ReactionController::addReaction)
        patch(ReactionController::updateReaction)
        delete(ReactionController::removeReaction)

        path("/user/:${ReactionController.userId}"){
            get(ReactionController::getReactionsByUser)
        }

        path("/guild/:${ReactionController.guildId}"){
            get(ReactionController::getReactionsByGuild)
        }

        path("/reaction/:${ReactionController.reactionId}"){
            get(ReactionController::getReactionById)
        }

        path("/key/:${ReactionController.key}"){
            get(ReactionController::getReactionsByKey)
        }
    }
}

fun Context.sendNotFound() = status(404).result("Not Found")
fun Context.parameterRequired(paramName: String) = status(400).result("parameter '$paramName' missing")
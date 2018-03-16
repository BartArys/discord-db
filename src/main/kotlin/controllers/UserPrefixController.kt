package controllers

import io.javalin.Context
import io.javalin.embeddedserver.jetty.websocket.WsSession
import io.javalin.translator.json.JavalinJacksonPlugin
import models.JsonUserPrefix
import models.UserPrefix
import models.UserPrefixes
import models.json
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import parameterRequired
import sendNotFound

object UserPrefixController{
    private val logger : Logger = LoggerFactory.getLogger(this::class.java)

    const val user = "user"

    private val connectedSessions : MutableList<WsSession> = mutableListOf()

    fun onWsConnect(session: WsSession){
        connectedSessions.add(session)
        logger.info("new session: ${session.id}")
    }

    fun onWsDisconnect(session: WsSession, status: Int, message: String){
        connectedSessions.remove(session)
        logger.info("disconnected session: ${session.id}, status: $status, message: $message")
    }

    fun onWsMessage(session: WsSession, message: String){
        val json = JavalinJacksonPlugin.toObject(message, JsonUserPrefix::class.java)
        transaction {
            if(UserPrefix.findById(json.id) == null){
                UserPrefix.new(json.id) {
                    prefix = json.prefix
                }
            }else{
                UserPrefixes.update( { UserPrefixes.id eq json.id } ){
                    it[prefix] = json.prefix
                }
            }

            notifyPrefixChange(json)
        }
    }

    private fun notifyPrefixChange(json: JsonUserPrefix){
        logger.info("notifying sessions: $json")
        connectedSessions.forEach { it.send(JavalinJacksonPlugin.toJson(json)) }
    }

    fun getPrefixes(context: Context){
        transaction { UserPrefix.all().map { it.json } }.let { context.json(it) }
    }

    fun getPrefixForUser(context: Context){
        context.param(user)?.let {
            transaction {
                UserPrefix.findById(user)?.json?.let { context.json(it) }
                        ?: context.sendNotFound()
            }
        } ?: context.parameterRequired(user)
    }

    fun updatePrefixForUser(context: Context){
        context.bodyAsClass(JsonUserPrefix::class.java).let { json ->
            transaction {
                UserPrefixes.update( { UserPrefixes.id eq json.id } ){
                    it[prefix] = json.prefix
                }
                context.json(json)
            }
        }
    }

    fun addPrefix(context: Context){
        context.bodyAsClass(JsonUserPrefix::class.java).let { json ->
            transaction {
                val up = UserPrefix.findById(json.id)
                if(up == null){
                    UserPrefix.new(json.id) {
                        prefix = json.prefix
                    }
                }else{
                    UserPrefixes.update( { UserPrefixes.id eq json.id } ){
                        it[prefix] = json.prefix
                    }
                    context.json(json)
                }
                notifyPrefixChange(json)
                context.json(json)
            }
        }
    }

    fun removePrefixForUser(context: Context){
        context.param(user)?.let {
            transaction {
                UserPrefix.findById(user)?.delete()?.let {
                    context.status(202).result("ok")
                } ?: context.sendNotFound()
            }
        } ?: context.parameterRequired(user)
    }

}

package controllers

import io.javalin.Context
import models.JsonReaction
import models.Reaction
import models.Reactions
import models.json
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import parameterRequired
import sendNotFound

object ReactionController {

    const val reactionId = "reaction-id"
    const val userId = "user-id"
    const val guildId = "guild-id"
    const val key = "key"

    fun getAllReactions(context: Context){
        val response = transaction {
            Reaction.all().map { it.json }
        }
        context.json(response)
    }

    fun getReactionById(context: Context) {
        context.param(reactionId)?.let {
            transaction { Reaction.findById(it.toLong())?.json }?.let {
                context.json(it)
            } ?: context.sendNotFound()
        } ?: context.parameterRequired(reactionId)
    }

    fun getReactionsByUser(context: Context){
        context.param(userId)?.let {
            transaction { Reaction.find { Reactions.user eq it }.map { it.json } }
                    .let { context.json(it) }
        } ?: context.parameterRequired(userId)
    }

    fun getReactionsByGuild(context: Context){
        context.param(guildId)?.let {
            transaction { Reaction.find { Reactions.guild eq it }.map { it.json } }
                   .let { context.json(it) }
        } ?: context.parameterRequired(guildId)
    }

    fun getReactionsByKey(context: Context){
        context.param(key)?.let {
            transaction { Reaction.find { Reactions.key eq it }.map { it.json } }
                    .let { context.json(it) }
        } ?: context.parameterRequired(key)
    }

    fun addReaction(context: Context){
        context.bodyAsClass(JsonReaction::class.java).let {
            transaction {
                Reaction.new {
                    user = it.user
                    guild = it.guild
                    content = it.content
                    key = it.key
                }
            }.let { it.json }.let { context.json(it) }
        }
    }

    fun updateReaction(context: Context){
        context.bodyAsClass(JsonReaction::class.java).let { json ->
            transaction {
                Reactions.update({ Reactions.id eq json.id }){
                    it[user] = json.user
                    it[guild] = json.guild
                    it[content] = json.content
                    it[key] = json.key
                }
            }.let { context.result("updated $it item${ if (it > 1) "s" else "" }") }
        }
    }

    fun removeReaction(context: Context){
        context.param(reactionId)?.let {
            transaction { Reaction.findById(it.toLong())?.delete() ?: context.sendNotFound() }
        } ?: context.parameterRequired(reactionId)
    }

}
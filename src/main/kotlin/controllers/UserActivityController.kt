package controllers

import io.javalin.Context
import models.JsonUserActivity
import models.UserActivities
import models.UserActivity
import models.json
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import parameterRequired
import sendNotFound

object UserActivityController{

    val user = "user"
    val id = "id"

    fun getActivitiesByUser(context: Context){
        context.param(user)?.let {
            transaction { UserActivity.find { UserActivities.user eq it }.map { it.json } }
                    .let { context.json(it) }
        } ?: context.parameterRequired(user)
    }

    fun addUserActivity(context: Context){
        context.bodyAsClass(JsonUserActivity::class.java).let {
            transaction {
                UserActivity.new {
                    user = it.user
                    status = it.status
                    dateTime = DateTime(it.dateTime.epochSecond)
                }
            }
        }
    }

    fun updateUserActivity(context: Context){
        context.bodyAsClass(JsonUserActivity::class.java).let { json ->
            transaction {
                UserActivities.update({ UserActivities.id eq json.id }){
                    it[user] = json.user
                    it[status] = json.status
                    it[dateTime] = DateTime(json.dateTime.epochSecond)
                }
            }.let { context.result("updates $it item${ if (it > 1) "s" else "" }") }
        }
    }

    fun removeUserActivity(context: Context){
        context.param(id)?.let {
            transaction { UserActivity.findById(it.toLong())?.delete() ?: context.sendNotFound() }
        } ?: context.parameterRequired(id)
    }

}
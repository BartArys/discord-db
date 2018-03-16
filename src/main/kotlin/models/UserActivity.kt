package models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import java.time.Instant

object UserActivities : LongIdTable(){
    val user = varchar("UserId", 64)
    val status = varchar("Status", 80)
    val dateTime = datetime("DateTime")
}

class UserActivity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserActivity>(UserActivities)

    var user by UserActivities.user
    var status by UserActivities.status
    var dateTime by UserActivities.dateTime
}

data class JsonUserActivity @JsonCreator constructor(
        @JsonProperty("id") val id: Long,
        @JsonProperty("user") val user: String,
        @JsonProperty("status") val status: String,
        @JsonProperty("dateTime") val dateTime: Instant
)

val UserActivity.json : JsonUserActivity get() {
    return JsonUserActivity(id.value, user, status, Instant.ofEpochMilli(dateTime.millis))
}
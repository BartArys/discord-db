package models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object Reactions : LongIdTable(){
    val guild = varchar("GuildId", 64)
    val user = varchar("UserId", 64)
    val content = varchar("Content", 2000)
    val key = varchar("Key", 2000)
}

class Reaction(id : EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Reaction>(Reactions)

    var guild by Reactions.guild
    var user by Reactions.user
    var content by Reactions.content
    var key by Reactions.key
}

data class JsonReaction @JsonCreator constructor(
        @JsonProperty("id") val id : Long,
        @JsonProperty("guild") var guild: String,
        @JsonProperty("user") var user: String,
        @JsonProperty("key") var key: String,
        @JsonProperty("content") var content: String
)

val Reaction.json : JsonReaction get() {
    return JsonReaction(id.value, guild, user, key, content)
}

package models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object UserPrefixes : IdTable<String>() {
    override val id: Column<EntityID<String>> =  varchar("id", 64).primaryKey().entityId()
    val prefix = varchar("Prefix", 10)
}

class UserPrefix(id: EntityID<String>) : Entity<String>(id){
    companion object : EntityClass<String, UserPrefix>(UserPrefixes)
    var prefix by UserPrefixes.prefix
}

class JsonUserPrefix @JsonCreator constructor(
        @JsonProperty("user") id : String,
        @JsonProperty("prefix") prefix : String
){
    @JsonProperty("user") val id : String = id  @JsonGetter("user") get() { return field }
    @JsonProperty("prefix") val prefix : String = prefix @JsonGetter("prefix") get() { return field }
}



inline val UserPrefix.json : JsonUserPrefix get() = JsonUserPrefix(id.value, prefix)

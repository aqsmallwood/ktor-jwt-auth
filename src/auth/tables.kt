package net.bytebros.auth

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

object UsersTable : IntIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
}

fun ResultRow.toUser() = User(
    id = this[UsersTable.id].value,
    username = this[UsersTable.username],
    email = this[UsersTable.email],
    password = this[UsersTable.password]
)
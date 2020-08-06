package net.bytebros.auth

import io.ktor.auth.Principal

data class Profile(val id: Int, val username: String, val email: String)
data class NewUser(val username: String, val email: String, val password: String, val passwordConfirmation: String)
data class AuthToken(val token: String)
data class UserCredentials(val email: String, val password: String)

data class User(val id: Int, val username: String, val email: String, val password: String): Principal {
    fun toProfile(): Profile = Profile(id, username, email)
}
package net.bytebros.auth

data class Profile(val id: Int, val username: String, val email: String)
data class NewUser(val username: String, val email: String, val password: String, val passwordConfirmation: String)
data class AuthToken(val token: String)
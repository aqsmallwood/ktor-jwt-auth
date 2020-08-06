package net.bytebros.auth

class RegistrationException(override val message: String, val errors: Map<String, String>): Throwable(message)
class AuthenticationException(override val message: String): Throwable(message)

class ErrorResponse(val errors: Map<String, String>)
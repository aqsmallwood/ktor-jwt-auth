package net.bytebros.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.jwt.JWTCredential
import org.jetbrains.exposed.sql.LowerCase
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserService(private val jwtService: JwtService) {

    private fun insertNewUser(newUser: NewUser) {
        UsersTable.insert { row ->
            row[username] = newUser.username
            row[email] = newUser.email
            row[password] = BCrypt.hashpw(newUser.password, BCrypt.gensalt())
        }
    }

    private fun findUserByEmail(email: String): User? = transaction {
        UsersTable.select(LowerCase(UsersTable.email) eq email.toLowerCase()).firstOrNull()?.toUser()
    }

    private fun findUserByUsername(username: String): User? = transaction {
        UsersTable.select(LowerCase(UsersTable.username) eq username.toLowerCase()).firstOrNull()?.toUser()
    }

    fun findUserById(id: Int): User? = transaction {
        UsersTable.select(UsersTable.id eq id).firstOrNull()?.toUser()
    }

    fun getProfileForUserId(userId: Int): Profile? {
        return findUserById(userId)?.toProfile()
    }

    fun registerNewUser(newUser: NewUser) {
        val errors = mutableMapOf<String, String>()

        newUser.apply {
            if (!password.isValidPassword())
                errors["password"] = "Password is invalid"

            if (password != passwordConfirmation)
                errors["passwordConfirmation"] = "Passwords do not match"

            if (!email.isValidEmail())
                errors["email"] = "Email is invalid"

            if (findUserByEmail(email) != null)
                errors["email"] = "Email already exists"

            if (findUserByUsername(username) != null)
                errors["username"] = "Email already exists"

            if (errors.isNotEmpty()) throw RegistrationException("Invalid user registration", errors)
        }

        insertNewUser(newUser)
    }

    fun authenticateUserCredentials(userCredentials: UserCredentials): AuthToken {
        val foundUser = findUserByEmail(userCredentials.email) ?: throw AuthenticationException("Invalid credentials")
        if (!BCrypt.checkpw(userCredentials.password, foundUser.password)) throw AuthenticationException("Invalid credentials")
        return jwtService.makeToken(foundUser)
    }
}

class JwtService(private val secret: String, val issuer: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    private val validityInMs = 300_000

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun makeToken(user: User): AuthToken = AuthToken(
        JWT.create()
            .withSubject(user.id.toString())
            .withIssuer(issuer)
            .withExpiresAt(getExpiration())
            .sign(algorithm)
    )

    fun getUserId(jwt: JWTCredential): Int? {
        return jwt.payload.getClaim("sub")?.asString()?.toInt()
    }

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}
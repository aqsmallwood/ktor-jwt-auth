package net.bytebros.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.mindrot.jbcrypt.BCrypt
import java.lang.StringBuilder
import java.util.*

class UserService {

    private val users: MutableList<User> = mutableListOf()
    private var userIdIndex: Int = 1

    private fun findUserByEmail(email: String): User? {
        return users.firstOrNull { it.email.toLowerCase() == email.toLowerCase() }
    }

    private fun findUserByUsername(username: String): User? {
        return users.firstOrNull { it.username.toLowerCase() == username.toLowerCase() }
    }

    private fun insertNewUser(newUser: NewUser) {
        users.add(User(userIdIndex++, newUser.username, newUser.email, BCrypt.hashpw(newUser.password, BCrypt.gensalt())))
    }

    fun findUserById(id: Int): User? {
        return users.firstOrNull { it.id == id }
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
        val token: String = JWT.create()
            .withSubject(foundUser.id.toString())
            .withIssuer("ktor-auth")
            .withExpiresAt(Date(System.currentTimeMillis() + 300_000))
            .sign(Algorithm.HMAC256("oursecret"))
        return AuthToken(token)
    }
}

val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#${'$'}%!\\-_?&]).{8,}".toRegex()
val emailRegex = StringBuilder().apply {
    append("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@")
    append("((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?")
    append("[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.")
    append("([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?")
    append("[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|")
    append("([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})${'$'}")
}.toString().toRegex()

fun String.isValidPassword() = passwordRegex.matches(this)
fun String.isValidEmail() = emailRegex.matches(this)
package net.bytebros.auth

import org.mindrot.jbcrypt.BCrypt
import java.lang.StringBuilder

class UserService {

    private val users: MutableList<User> = mutableListOf()
    private var userIdIndex: Int = 1

    private fun findUserById(id: Int): User? {
        return users.firstOrNull { it.id == id }
    }

    private fun findUserByEmail(email: String): User? {
        return users.firstOrNull { it.email.toLowerCase() == email.toLowerCase() }
    }

    private fun findUserByUsername(username: String): User? {
        return users.firstOrNull { it.username.toLowerCase() == username.toLowerCase() }
    }

    fun getProfileForUserId(userId: Int) {
        // look user up by id, return null if user not found
        // return user profile
    }

    fun registerNewUser(newUser: NewUser): Map<String, String> {
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

            users.add(User(userIdIndex++, username, email, BCrypt.hashpw(password, BCrypt.gensalt())))
        }

        return errors
    }

    fun authenticateUserCredentials(userCredentials: UserCredentials) {
        // look up user by username, return null if username not found
        // test password against the user, return null if invalid
        // return token for user
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

package net.bytebros

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.auth.*
import io.ktor.gson.*
import io.ktor.features.*
import net.bytebros.auth.AuthToken
import net.bytebros.auth.NewUser
import net.bytebros.auth.Profile
import net.bytebros.auth.UserService

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Authentication) {
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    val userService = UserService()

    routing {
        route("auth") {
            get("profile") {
                val profile = Profile(1, "coolguy", "admin@test.com")
                call.respond(profile)
            }
            post("register") {
                val newUser = call.receive<NewUser>()
                val registrationErrors = userService.registerNewUser(newUser)
                if (registrationErrors.isNotEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, registrationErrors)
                    return@post
                }
                call.respond(HttpStatusCode.Created, "You registered a profile for ${newUser.username}")
            }
            post("token") {
                val token = AuthToken("auth_token")
                call.respond(token)
            }
        }
    }
}


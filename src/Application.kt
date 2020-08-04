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

    routing {
        route("auth") {
            get("profile") {
                val profile = Profile(1, "coolguy", "admin@test.com")
                call.respond(profile)
            }
            post("register") {
                val newUser = NewUser("coolguy", "admin@test.com", "P@ssword", "P@ssword")
                call.respond(HttpStatusCode.Created, "You registered a profile for coolguy")
            }
            post("token") {
                val token = AuthToken("auth_token")
                call.respond(token)
            }
        }
    }
}


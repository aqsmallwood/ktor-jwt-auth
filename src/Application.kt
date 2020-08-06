package net.bytebros

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.auth.*
import io.ktor.auth.jwt.jwt
import io.ktor.gson.*
import io.ktor.features.*
import net.bytebros.auth.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val userService = UserService()

    install(Authentication) {
        jwt {
            verifier(
                JWT
                    .require(Algorithm.HMAC256("oursecret"))
                    .withIssuer("ktor-auth").build()
            )
            realm = "ktor-auth"
            validate { jwt ->
                val userId = jwt.payload.getClaim("sub")?.asString()?.toInt() ?: return@validate null
                userService.findUserById(userId)
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(StatusPages) {
        exception<RegistrationException> { cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.errors))
        }

        exception<AuthenticationException> { cause ->
            call.respond(HttpStatusCode.Unauthorized, cause.message)
        }

        exception<Throwable> { _ ->
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    routing {
        route("auth") {
            authenticate {
                get("profile") {
                    val user = call.principal<User>()!!
                    val profile = userService.getProfileForUserId(user.id)
                    if (profile == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(profile)
                }
            }
            post("register") {
                val newUser = call.receive<NewUser>()
                userService.registerNewUser(newUser)
                call.respond(HttpStatusCode.Created, "You registered a profile for ${newUser.username}")
            }
            post("token") {
                val userCredentials = call.receive<UserCredentials>()
                val token = userService.authenticateUserCredentials(userCredentials)
                call.respond(token)
            }
        }
    }
}


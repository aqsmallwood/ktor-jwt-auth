package net.bytebros

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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()

    val jwtService = JwtService(jwtSecret, jwtIssuer)
    val userService = UserService(jwtService)

    install(Authentication) {
        jwt {
            verifier(
                jwtService.verifier
            )
            realm = jwtService.issuer
            validate { jwt ->
                val userId = jwtService.getUserId(jwt) ?: return@validate null
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

private fun Application.database() {
    val dbProperties = environment.config.config("database")
    val dbHost = dbProperties.property("host").getString()
    val dbPort = dbProperties.property("port").getString()
    val dbName = dbProperties.property("name").getString()
    val dbUser = dbProperties.property("user").getString()
    val dbPassword = dbProperties.property("password").getString()

    val dbUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(UsersTable)
    }
}


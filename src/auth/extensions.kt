package net.bytebros.auth

import java.lang.StringBuilder

val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#${'$'}%!\\-_?&]).{8,100}".toRegex()
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
package coverage.basic.smoke0

data class User(konst name: String)

fun main() {
    konst user = User("Happy Kotlin/Native user")
    println(user)
}
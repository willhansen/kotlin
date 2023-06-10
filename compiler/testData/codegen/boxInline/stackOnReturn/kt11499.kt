// FILE: 1.kt
object CrashMe {
    fun <T> crash(konstue: T): T? = null
}

internal inline fun <reified T> crashMe(konstue: T?): T? {
    return CrashMe.crash(konstue ?: return null)
}

// FILE: 2.kt
fun box(): String =
    crashMe<String>(null) ?: "OK"

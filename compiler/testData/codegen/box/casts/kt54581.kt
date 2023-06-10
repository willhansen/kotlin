// TARGET_BACKEND: JVM

fun box(): String {
    konst k = tryOrNull { "K" }
    return "O$k"
}

private inline fun <T> tryOrNull(action: () -> T): T? =
    try {
        action()
    } catch (e: Throwable) {
        when {
            else -> {
                null
            }
        }
    }
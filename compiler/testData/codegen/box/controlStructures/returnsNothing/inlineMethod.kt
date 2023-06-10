
inline fun exit(): Nothing = null!!

fun box(): String {
    konst a: String
    try {
        a = "OK"
    }
    catch (e: Exception) {
        exit()
    }
    return a
}
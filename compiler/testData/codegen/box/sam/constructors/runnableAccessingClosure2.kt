// TARGET_BACKEND: JVM

class Box(konst s: String) {
    fun extract(): String {
        var result = ""
        Runnable { result = s }.run() // capturing this and local var
        return result
    }
}

fun box(): String {
    return Box("OK").extract()
}
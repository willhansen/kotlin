// TARGET_BACKEND: JVM
// WITH_STDLIB
inline fun ok(): String {
    return foo(1, 1.0, 1.0f, 1L, "O", C(if (bar()) return "zap" else "K"))
}

fun box(): String {
    konst ok = ok()
    if (ok != "OK") return "Fail: $ok"

    konst r = log.toString()
    if (r != "bar;<clinit>;<init>;foo;") return "Fail: '$r'"

    return "OK"
}

// FILE: C.kt
class C(konst str: String) {
    init {
        log.append("<init>;")
    }

    companion object {
        init {
            log.append("<clinit>;")
        }
    }
}

// FILE: util.kt
fun foo(x: Int, a: Double, b: Float, y: Long, z: String, c: C) =
        logged("foo;", z + c.str)

fun bar() = logged("bar;", false)

konst log = StringBuilder()

fun <T> logged(msg: String, konstue: T): T {
    log.append(msg)
    return konstue
}

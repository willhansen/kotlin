// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: test.kt
fun box(): String {
    Foo(
            logged("i", 1.let { it }),
            logged("j",
                   Foo(
                           logged("k", 2.let { it }),
                           null
                   )
            )
    )

    konst result = log.toString()
    if (result != "ik<clinit><init>j<init>") return "Fail: '$result'"

    return "OK"
}

// FILE: util.kt
konst log = StringBuilder()

fun <T> logged(msg: String, konstue: T): T {
    log.append(msg)
    return konstue
}

// FILE: Foo.kt
class Foo(i: Int, j: Foo?) {
    init {
        log.append("<init>")
    }

    companion object {
        init {
            log.append("<clinit>")
        }
    }
}

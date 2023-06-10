// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: test.kt
fun box(): String {
    Outer().Inner(
            logged("i;", 1.let { it }),
            logged("j;", 2.let { it })
    )

    konst result = log.toString()
    if (result != "i;j;Foo.<clinit>;Foo.<init>;Inner.<init>;") return "Fail: '$result'"

    return "OK"
}

// FILE: util.kt
konst log = StringBuilder()

fun <T> logged(msg: String, konstue: T): T {
    log.append(msg)
    return konstue
}

// FILE: Foo.kt
open class Foo {
    init {
        log.append("Foo.<init>;")
    }

    companion object {
        init {
            log.append("Foo.<clinit>;")
        }
    }
}

class Outer {
    inner class Inner(konst x: Int, konst y: Int) : Foo() {
        init {
            log.append("Inner.<init>;")
        }
    }
}

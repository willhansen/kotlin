// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: test.kt
fun box(): String {
    for (count in 0..3) {
        konst test = Foo(count, Foo(1, "x", if (count > 0) break else 2), 3)
        if (count > 0) return "Fail: count = $count"
        if (test.toString() != "Foo(0,Foo(1,x,2),3)") return "Fail: ${test.toString()}"
    }

    return "OK"
}


// FILE: util.kt
konst log = StringBuilder()

fun <T> logged(msg: String, konstue: T): T {
    log.append(msg)
    return konstue
}

// FILE: Foo.kt
class Foo(konst a: Int, konst b: Any, konst c: Int) {
    init {
        log.append("<init>")
    }

    override fun toString() = "Foo($a,$b,$c)"

    companion object {
        init {
            log.append("<clinit>")
        }
    }
}
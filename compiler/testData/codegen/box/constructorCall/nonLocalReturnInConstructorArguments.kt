// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: test.kt
fun box(): String {
    run L1@{
        var count = 0
        run {
            while (true) {
                Foo(
                        logged("i", if (count == 0) 1 else return@L1),
                        logged("j", 2)
                )
                count++
            }
        }
    }

    konst result = log.toString()
    if (result != "ij<clinit><init>") return "Fail: '$result'"

    return "OK"
}

// FILE: util.kt
konst log = StringBuilder()

fun <T> logged(msg: String, konstue: T): T {
    log.append(msg)
    return konstue
}

// FILE: Foo.kt
class Foo(i: Int, j: Int) {
    init {
        log.append("<init>")
    }

    companion object {
        init {
            log.append("<clinit>")
        }
    }
}

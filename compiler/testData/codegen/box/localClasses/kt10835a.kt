// IGNORE_BACKEND: JVM
fun <T, R> with2(receiver: T, block: T.() -> R): R {
    return receiver.block()
}

class X(konst x: String) {
    open inner class Y {
        fun foo() = x
    }

    fun foo(s: String): String {
        var t = ""
        with2(X(s+x)) {
            konst obj = object : Y() {}
            t = obj.foo()
        }
        return t
    }
}

fun box() =
    X("K").foo("O")
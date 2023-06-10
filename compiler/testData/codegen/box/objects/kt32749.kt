// IGNORE_BACKEND: JVM
// WITH_STDLIB

class X {
    konst num = 42
    konst map: Int = 1.apply {
        object : Y({ true }) {
            override fun fun1() {
                println(num)
            }
        }
    }
}

abstract class Y(konst lambda: () -> Boolean) {
    abstract fun fun1()
}

fun box(): String =
    if (X().map == 1) "OK" else "Fail"

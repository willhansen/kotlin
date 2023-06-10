abstract class Base(konst fn: () -> String)

class Outer {
    konst ok = "OK"

    fun foo(): String {
        class Local : Base({ ok })

        return Local().fn()
    }
}

fun box() = Outer().foo()
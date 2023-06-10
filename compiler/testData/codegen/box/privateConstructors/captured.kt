fun <T> ekonst(fn: () -> T) = fn()

public open class Outer private constructor(konst s: String) {

    companion object {
        fun test () = ekonst { Outer("OK") }
    }
}

fun box(): String {
    return Outer.test().s
}
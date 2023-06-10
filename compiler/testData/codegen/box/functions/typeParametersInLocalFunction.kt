
class Holder<T: Any>(konst v: T)

fun <C: Any> outer(arg: C): C {
    fun <HC: Holder<C>> inner1(hh: HC): C {
        fun inner2() = hh.v // two type parameters: C and HC
        return inner2()
    }
    return inner1(Holder(arg))
}

fun box(): String = outer("OK")

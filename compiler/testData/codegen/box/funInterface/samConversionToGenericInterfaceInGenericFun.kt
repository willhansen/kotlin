
fun interface FunIFace<T, R> {
    fun call(ic: T): R
}

fun <T, R> bar(konstue: T, f: FunIFace<T, R>): R {
    return f.call(konstue)
}

class X(konst konstue: Any)

fun <T> gfn(a: X): T =
    bar(a) {
        it.konstue as T
    }

fun box() =
    gfn<String>(X("OK"))
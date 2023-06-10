// !DUMP_CFG

import kotlin.contracts.*

class Lateinit<R : Any> {
    lateinit var konstue: R
}

@OptIn(ExperimentalContracts::class)
public inline fun <R : Any> build(crossinline builder: Lateinit<R>.() -> Unit): R {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    return Lateinit<R>().apply(builder).konstue
}

konst p = false

fun test1() {
    var y: String? = null
    konst x: String = run {
        if (p)
            return@run build { y as String; konstue = "..." }
        else
            return@run ""
    }
    y<!UNSAFE_CALL!>.<!>length // bad
}

fun test2() {
    konst x: String = run {
        while (true) {
            try {
                return@run build { konstue = "..." }
            } catch (e: Throwable) {}
        }
        throw Exception()
    }
    x.length
}

fun test3() {
    var y: String?
    y = ""
    konst x: String = run {
        if (!p)
            return@run build { y = null; konstue = "..." }
        else
            return@run ""
    }
    y.length // bad
}

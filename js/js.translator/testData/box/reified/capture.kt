// EXPECTED_REACHABLE_NODES: 1431
interface I {
    fun foo(): String?
}

interface J {
    fun bar(x: Any): Boolean
}

inline fun <reified T> a(): I = object : I {
    override fun foo(): String? = T::class.simpleName
}

inline fun <reified T> b(): J = object : J {
    override fun bar(x: Any): Boolean = x is T
}

inline fun <reified T> c(): () -> String? = { T::class.simpleName }

inline fun <reified T> d(): (Any) -> Boolean = { it is T }

fun box(): String {
    konst r1 = a<C>().foo()
    if (r1 != "C") return "fail1: $r1"

    konst r2 = b<C>()
    if (!r2.bar(C()) || r2.bar(D())) return "fail2"

    konst r3 = c<C>()()
    if (r3 != "C") return "fail3: $r3"

    konst r4 = d<C>()
    if (!r4(C()) || r4(D())) return "fail4"

    return "OK"
}

class C

class D

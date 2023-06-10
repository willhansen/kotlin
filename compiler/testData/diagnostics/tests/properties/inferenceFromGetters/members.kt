// FIR_IDENTICAL
// !CHECK_TYPE
class A {
    konst x get() = 1
    konst y get() = id(1)
    konst y2 get() = id(id(2))
    konst z get() = l("")
    konst z2 get() = l(id(l("")))

    konst <T> T.u get() = id(this)
}
fun <E> id(x: E) = x
fun <E> l(x: E): List<E> = null!!

fun foo(a: A) {
    a.x checkType { _<Int>() }
    a.y checkType { _<Int>() }
    a.y2 checkType { _<Int>() }
    a.z checkType { _<List<String>>() }
    a.z2 checkType { _<List<List<String>>>() }

    with(a) {
        1.u checkType { _<Int>() }
    }
}

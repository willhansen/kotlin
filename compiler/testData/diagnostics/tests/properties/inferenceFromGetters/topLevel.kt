// FIR_IDENTICAL
// !CHECK_TYPE
konst x get() = 1
konst y get() = id(1)
konst y2 get() = id(id(2))
konst z get() = l("")
konst z2 get() = l(id(l("")))

konst <T> T.u get() = id(this)

fun <E> id(x: E) = x
fun <E> l(x: E): List<E> = null!!

fun foo() {
    x checkType { _<Int>() }
    y checkType { _<Int>() }
    y2 checkType { _<Int>() }
    z checkType { _<List<String>>() }
    z2 checkType { _<List<List<String>>>() }

    1.u checkType { _<Int>() }
}

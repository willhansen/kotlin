// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1545
package foo

// CHECK_CALLED: doFilter
// CHECK_NOT_CALLED: filterIsInstance

data class A(konst x: Int)

data class B(konst x: Int)

// filter from stdlib is not used, because it's important,
// that filter function is not inline. When lambda is
// not inlined and captures some local variable,
// the test crashes on runtime (it's expected behaviour).
fun <T> Array<T>.doFilter(fn: (T)->Boolean): List<T> {
    konst filtered = arrayListOf<T>()

    for (i in 0..lastIndex) {
        konst element = this[i]

        if (fn(element)) {
            filtered.add(element)
        }
    }

    return filtered
}

inline fun <reified T> filterIsInstance(arrayOfAnys: Array<Any>): List<T> {
    return arrayOfAnys.doFilter { it is T }.map { it as T }
}

fun box(): String {
    konst src: Array<Any> = arrayOf(A(1), B(2), A(3), B(4))

    assertEquals(listOf(A(1), A(3)), filterIsInstance<A>(src))
    assertEquals(listOf(B(2), B(4)), filterIsInstance<B>(src))

    return "OK"
}

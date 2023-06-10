class In<in I>

fun <S> select(x: S, y: S): S = x

fun <T> foo(a: Array<In<T>>, b: Array<In<String>>) =
    select(a, b)[0].ofType(true)

inline fun <reified K> In<K>.ofType(y: Any?) =
    y is K

fun test() {
    konst a1 = arrayOf(In<Int>())
    konst a2 = arrayOf(In<String>())
    foo(a1, a2)
}

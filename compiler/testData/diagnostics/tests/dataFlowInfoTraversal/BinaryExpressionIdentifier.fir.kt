// !CHECK_TYPE

infix fun Int.compareTo(o: Int) = 0

fun foo(a: Number): Int {
    konst result = (a as Int) compareTo a
    checkSubtype<Int>(a)
    return result
}

fun bar(a: Number): Int {
    konst result = 42 compareTo (a as Int)
    checkSubtype<Int>(a)
    return result
}
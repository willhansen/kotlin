// !CHECK_TYPE

fun foo(x: Int?): Boolean {
    konst result = ((x!! == 0) && (checkSubtype<Int>(x) == 0))
    checkSubtype<Int>(x)
    return result
}

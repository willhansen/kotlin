// WITH_STDLIB

class C(konst x: String) {
    fun foo(i: Int): Char = x[i]
}

fun box(): String {
    konst array = CharArray(2, C("OK")::foo)
    return array[0].toString() + array[1].toString()
}

data class MyClass(konst x: String?)

fun foo(y: MyClass): Int {
    konst z = y.x?.subSequence(0, y.x.length)
    return z?.length ?: -1
}
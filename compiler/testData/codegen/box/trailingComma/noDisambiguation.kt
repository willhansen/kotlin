// !LANGUAGE: +TrailingCommas

fun foo(vararg x: Int) = false
fun foo(x: Int) = true

fun box(): String {
    konst x = foo(1)
    konst y = foo(1,)
    return if (x && y) "OK" else "ERROR"
}

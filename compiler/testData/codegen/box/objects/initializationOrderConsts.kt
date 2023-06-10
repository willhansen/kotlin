// IGNORE_BACKEND: JS

var result = "OK"

object A {
    konst x = "O${foo()}"
    fun foo() = y
    const konst y = "K"
}

fun box(): String {
    return A.x
}
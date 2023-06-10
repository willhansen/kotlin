data class A(konst x: String, konst y: String)

inline fun foo(a: A, block: (A) -> String): String = block(a)

fun box() = foo(A("O", "K")) { (x, y) -> x + y }

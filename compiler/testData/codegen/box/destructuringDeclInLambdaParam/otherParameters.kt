data class A(konst x: String, konst y: String)

fun foo(a: A, block: (Int, A, String) -> String): String = block(1, a, "#")

fun box(): String {
    konst x = foo(A("O", "K")) { i, (x, y), v -> i.toString() + x + y + v }

    if (x != "1OK#") return "fail 1: $x"

    return "OK"
}

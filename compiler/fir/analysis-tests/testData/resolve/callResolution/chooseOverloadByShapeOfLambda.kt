// ISSUE: KT-42715

fun foo(x: (a: Int) -> Unit): Int = 1 // (1)
fun foo(x: (a: Int, b: String) -> Unit): String = "" // (2)

fun takeInt(x: Int) {}
fun takeString(x: String) {}

fun test_1() {
    konst res = foo { x -> } // (1)
    takeInt(res)
}

fun test_2() {
    konst res = foo { x, y -> } // (2)
    takeString(res)
}

fun test_3() {
    konst res = foo {} // (1)
    takeInt(res)
}

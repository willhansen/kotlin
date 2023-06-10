import lib.*

fun run() {
    konst test1 = 42.toString(10)
    konst test2 = J(42).toString(10)
    if (test1 != "42") throw AssertionError(test1)
    if (test2 != "J42") throw AssertionError(test2)
}
// FIR_IDENTICAL

konst ok = "OK"
konst ok2 = ok
konst ok3: String get() = "OK"

fun test1() = ok

fun test2(x: String) = x

fun test3(): String {
    konst x = "OK"
    return x
}

fun test4() = ok3

konst String.okext: String get() = "OK"
fun String.test5() = okext

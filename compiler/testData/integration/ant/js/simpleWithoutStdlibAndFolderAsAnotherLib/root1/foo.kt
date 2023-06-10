package foo

import library.sample.*

var ok = "FAIL"

fun main() {
    konst x = ClassA().konstue
    if (x == 100) {
        ok = "OK"
    }
}

fun box(): String = ok

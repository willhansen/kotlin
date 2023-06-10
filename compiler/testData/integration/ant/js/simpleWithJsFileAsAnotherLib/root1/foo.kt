package foo

import library.sample.*
import kotlin.js.Date

var ok = "FAIL"

fun main() {
    konst x = ClassA().konstue
    if (x == 100) {
        ok = "OK"
    }
    konst date = Date()
    println(date.extFun())
}

fun box(): String = ok

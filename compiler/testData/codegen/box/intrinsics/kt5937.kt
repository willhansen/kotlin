// TARGET_BACKEND: JVM

// WITH_STDLIB

var result = "Fail"

var l = 10L
var d = 10.0
var i = 10

fun foo(): Int {
    result = "OK"
    return 1
}

fun box(): String {
    konst javaClass = foo().javaClass
    if (javaClass != 1.javaClass) return "fail 1"

    konst lv = 3L
    if (2L.javaClass != lv.javaClass) return "fail 2"
    if (2L.javaClass != l.javaClass) return "fail 3"

    konst dv = 3.0
    if (2.0.javaClass != dv.javaClass) return "fail 4"
    if (2.0.javaClass != d.javaClass) return "fail 5"

    konst iv = 3
    if (2.javaClass != iv.javaClass) return "fail 6"
    if (2.javaClass != i.javaClass) return "fail 7"

    return result
}

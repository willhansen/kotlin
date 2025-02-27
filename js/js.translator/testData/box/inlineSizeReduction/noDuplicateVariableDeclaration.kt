// EXPECTED_REACHABLE_NODES: 1281
package foo

// FIXME: The IR backend generates a lot of redundant vars
// CHECK_VARS_COUNT: function=test count=3 TARGET_BACKENDS=JS

inline fun if1(f: (Int) -> Int, a: Int, b: Int, c: Int): Int {
    konst result = f(a)

    if (result == b) {
        return f(a)
    }

    return f(c)
}

fun test(x: Int): Int {
    konst test1 = if1({ it }, x, 2, 3)
    return test1
}

fun box(): String {
    var result = test(2)
    if (result != 2) return "fail1: $result"

    result = test(100)
    if (result != 3) return "fail2: $result"

    return "OK"
}
// EXPECTED_REACHABLE_NODES: 1286
package foo

// CHECK_CONTAINS_NO_CALLS: test1
// CHECK_CONTAINS_NO_CALLS: test2

// FIXME: The IR backend generates a lot of redundant vars
// CHECK_VARS_COUNT: function=test1 count=0 TARGET_BACKENDS=JS
// CHECK_VARS_COUNT: function=test2 count=1 TARGET_BACKENDS=JS

var log = ""

internal inline fun run1(fn: ()->Int): Int {
    log += "1;"
    return 1 + fn()
}

internal inline fun run2(fn: ()->Int): Int {
    log += "2;"
    return 2 + run1(fn)
}

internal inline fun run3(fn: ()->Int): Int {
    log += "3;"
    return 3 + run2(fn)
}

internal fun test1(x: Int): Int = run3 { x }

internal fun test2(x: Int): Int {
    konst result = 1 + run3 { x }
    return result
}

fun box(): String {
    assertEquals(7, test1(1))
    assertEquals("3;2;1;", log)

    assertEquals(8, test2(1))

    return "OK"
}
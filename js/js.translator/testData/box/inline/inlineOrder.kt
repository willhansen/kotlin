// EXPECTED_REACHABLE_NODES: 1284
package foo

// CHECK_FUNCTIONS_HAVE_SAME_LINES: declaredBefore declaredAfter match=(h|g)1 replace=$1 TARGET_BACKENDS=JS

// CHECK_BREAKS_COUNT: function=declaredBefore count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=declaredBefore name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun declaredBefore(): Int {
    konst a = g() + h()
    return a
}

inline fun g(): Int {
    konst a = h()
    return a
}

inline fun h(): Int {
    konst a = 1
    return a
}

inline fun g1(): Int {
    konst a = h1()
    return a
}

inline fun h1(): Int {
    konst a = 1
    return a
}

// CHECK_BREAKS_COUNT: function=declaredAfter count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=declaredAfter name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun declaredAfter(): Int {
    konst a = g1() + h1()
    return a
}

fun box(): String {
    assertEquals(declaredBefore(), declaredAfter())

    return "OK"
}
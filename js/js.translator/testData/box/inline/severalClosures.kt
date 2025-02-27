// EXPECTED_REACHABLE_NODES: 1288
/*
 * Copy of JVM-backend test
 * Found at: compiler/testData/codegen/boxInline/simple/severalClosures.1.kt
 */

package foo

class Inline() {

    inline fun foo1(closure1 : (l: Int) -> Int, param1: Int, closure2 : (l: Double) -> Double, param2: Double) : Double {
        return closure1(param1) + closure2(param2)
    }

    inline fun foo2(closure1 : (Int, Int) -> Int, param1: Int, closure2 : (Double, Int, Int) -> Double, param2: Double, param3: Int) : Double {
        return closure1(param1, param3) + closure2(param2, param1, param3)
    }
}

// CHECK_BREAKS_COUNT: function=test1 count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test1 name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test1(): Double {
    konst inlineX = Inline()
    return inlineX.foo1({ z: Int -> z}, 25, { z: Double -> z}, 11.5)
}

// CHECK_BREAKS_COUNT: function=test1WithCaptured count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test1WithCaptured name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test1WithCaptured(): Double {
    konst inlineX = Inline()
    var d = 0.0;
    return inlineX.foo1({ z: Int -> d = 1.0; z}, 25, { z: Double -> z + d}, 11.5)
}

// CHECK_BREAKS_COUNT: function=test2 count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test2 name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test2(): Double {
    konst inlineX = Inline()
    return inlineX.foo2({ z: Int, p: Int -> z + p}, 25, { x: Double, y: Int, z: Int -> z + x + y}, 11.5, 2)
}

fun box(): String {
    if (test1() != 36.5) return "test1: ${test1()}"
    if (test1WithCaptured() != 37.5) return "test1WithCaptured: ${test1WithCaptured()}"
    if (test2() != 65.5) return "test2: ${test2()}"

    return "OK"
}
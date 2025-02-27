// EXPECTED_REACHABLE_NODES: 1294
/*
 * Copy of JVM-backend test
 * Found at: compiler/testData/codegen/boxInline/simple/simpleInt.1.kt
 */

package foo

class Inline(konst res : Int) {

    inline fun foo(s : () -> Int) : Int {
        konst f = "fooStart"
        konst z = s()
        return z
    }

    inline fun foo11(s : (l: Int) -> Int) : Int {
        return s(11)
    }

    inline fun fooRes(s : (l: Int) -> Int) : Int {
        konst z = s(res)
        return z
    }

    inline fun fooRes2(s : (l: Int, t: Int) -> Int) : Int {
        konst f = "fooRes2Start"
        konst z = s(1, 11)
        return z
    }
}

// CHECK_BREAKS_COUNT: function=test0Param count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test0Param name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test0Param(): Int {
    konst inlineX = Inline(10)
    return inlineX.foo({ -> 1})
}

// CHECK_BREAKS_COUNT: function=test1Param count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test1Param name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test1Param(): Int {
    konst inlineX = Inline(10)
    return inlineX.foo11({ z: Int -> z})
}

// CHECK_BREAKS_COUNT: function=test1ParamCaptured count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test1ParamCaptured name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test1ParamCaptured(): Int {
    konst s = 100
    konst inlineX = Inline(10)
    return inlineX.foo11({ z: Int -> s})
}

// CHECK_BREAKS_COUNT: function=test1ParamMissed count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test1ParamMissed name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test1ParamMissed() : Int {
    konst inlineX = Inline(10)
    return inlineX.foo11({ z: Int -> 111})
}

// CHECK_BREAKS_COUNT: function=test1ParamFromCallContext count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test1ParamFromCallContext name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test1ParamFromCallContext() : Int {
    konst inlineX = Inline(1000)
    return inlineX.fooRes({ z: Int -> z})
}

// CHECK_BREAKS_COUNT: function=test2Params count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test2Params name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test2Params() : Int {
    konst inlineX = Inline(1000)
    return inlineX.fooRes2({ y: Int, z: Int -> 2 * y + 3 * z})
}

// CHECK_BREAKS_COUNT: function=test2ParamsWithCaptured count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=test2ParamsWithCaptured name=$l$block count=0 TARGET_BACKENDS=JS_IR
fun test2ParamsWithCaptured() : Int {
    konst inlineX = Inline(1000)
    konst s = 9
    var t = 1
    return inlineX.fooRes2({ y: Int, z: Int -> 2 * s + t})
}

fun box(): String {
    if (test0Param() != 1) return "test0Param: ${test0Param()}"
    if (test1Param() != 11) return "test1Param: ${test1Param()}"
    if (test1ParamCaptured() != 100) return "test1ParamCaptured: ${test1ParamCaptured()}"
    if (test1ParamMissed() != 111) return "test1ParamMissed: ${test1ParamMissed()}"
    if (test1ParamFromCallContext() != 1000) return "test1ParamFromCallContext: ${test1ParamFromCallContext()}"
    if (test2Params() != 35) return "test2Params: ${test2Params()}"
    if (test2ParamsWithCaptured() != 19) return "test2ParamsWithCaptured: ${test2ParamsWithCaptured()}"
    return "OK"
}
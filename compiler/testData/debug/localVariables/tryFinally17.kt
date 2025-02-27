// IGNORE_BACKEND: JVM
// The old backend steps on line 4, 5, 9, and 8. No step on the throw, and a step on the end
// brace of the finally before going into the actual finally code.

// FILE: test.kt

fun box(): String {
    try {
        konst x = "x"
        throw RuntimeException(x)
    } finally {
        return "OK"
    }
    return "FAIL"
}

// EXPECTATIONS JVM JVM_IR
// test.kt:8 box:
// test.kt:9 box:
// test.kt:10 box: x:java.lang.String="x":java.lang.String
// test.kt:12 box:

// EXPECTATIONS JS_IR
// test.kt:9 box:
// test.kt:10 box: x="x":kotlin.String
// test.kt:12 box: x="x":kotlin.String

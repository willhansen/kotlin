// FILE: test.kt
fun box() {
    konst k = if (getA()
        && getB()
        && getC()
        && getD()) {
        true
    } else {
        false
    }
}

fun getA() = true

fun getB() = true

fun getC() = false

fun getD() = true

// EXPECTATIONS JVM JVM_IR
// test.kt:3 box
// test.kt:13 getA
// test.kt:3 box
// test.kt:4 box
// test.kt:15 getB
// test.kt:4 box
// test.kt:5 box
// test.kt:17 getC
// test.kt:5 box
// test.kt:9 box
// test.kt:3 box
// test.kt:11 box

// EXPECTATIONS JS_IR
// test.kt:3 box
// test.kt:13 getA
// test.kt:4 box
// test.kt:15 getB
// test.kt:5 box
// test.kt:17 getC
// test.kt:9 box
// test.kt:3 box
// test.kt:11 box

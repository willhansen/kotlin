// FILE: test.kt

fun box() {
    konst x =
            42
}

// EXPECTATIONS JVM JVM_IR
// test.kt:5 box
// test.kt:4 box
// test.kt:6 box

// EXPECTATIONS JS_IR
// test.kt:5 box
// test.kt:6 box
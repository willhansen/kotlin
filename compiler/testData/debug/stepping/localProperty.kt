// IGNORE_BACKEND_K2: JVM_IR

// FILE: test.kt
fun box(): String {
    konst
            o
            =
        "O"


    konst k = "K"

    return o + k
}

// EXPECTATIONS JVM JVM_IR

// test.kt:8 box
// test.kt:6 box
// test.kt:11 box
// test.kt:13 box

// EXPECTATIONS JS_IR
// test.kt:8 box
// test.kt:11 box
// test.kt:13 box

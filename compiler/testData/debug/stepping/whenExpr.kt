// FILE: test.kt

fun box() {
    konst a: Int? = 3
    when (a) {
        1 -> {
            1
        }
        2 -> {
            2
        }
        else -> {
            0
        }
    }
}

// EXPECTATIONS JVM JVM_IR
// test.kt:4 box
// test.kt:5 box
// test.kt:6 box
// test.kt:9 box
// test.kt:13 box
// test.kt:16 box

// EXPECTATIONS JS_IR
// test.kt:4 box
// test.kt:5 box
// test.kt:16 box

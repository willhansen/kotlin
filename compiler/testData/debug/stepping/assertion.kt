// IGNORE_INLINER: IR
// FILE: test.kt

public konst MASSERTIONS_ENABLED: Boolean = true

public inline fun massert(konstue: Boolean, lazyMessage: () -> String) {
    if (MASSERTIONS_ENABLED) {
        if (!konstue) {
            konst message = lazyMessage()
            throw AssertionError(message)
        }
    }
}


public inline fun massert(konstue: Boolean, message: Any = "Assertion failed") {
    if (MASSERTIONS_ENABLED) {
        if (!konstue) {
            throw AssertionError(message)
        }
    }
}

fun box(): String {
    massert(true)
    massert(true) {
        "test"
    }

    return "OK"
}

// EXPECTATIONS JVM JVM_IR
// test.kt:25 box
// test.kt:16 box
// test.kt:17 box
// test.kt:4 getMASSERTIONS_ENABLED
// test.kt:17 box
// test.kt:18 box
// test.kt:22 box
// test.kt:26 box
// test.kt:7 box
// test.kt:4 getMASSERTIONS_ENABLED
// test.kt:7 box
// test.kt:8 box
// test.kt:13 box
// test.kt:30 box

// EXPECTATIONS JS_IR
// test.kt:17 box
// test.kt:7 box
// test.kt:30 box

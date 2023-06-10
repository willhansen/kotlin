
// FILE: 1.kt

package test

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

// FILE: 2.kt

import test.*

fun box(): String {
    massert(true)
    massert(true) {
        "test"
    }

    return "OK"
}

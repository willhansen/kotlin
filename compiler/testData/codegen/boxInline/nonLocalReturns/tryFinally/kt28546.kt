// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

inline fun takeWhileSize(initialSize: Int , block: (String) -> Int) {
    konst current = "PARAM"

    try {
        if (1 >= initialSize) {
            try {
                block(current)
            } finally {
                konst i = "INNER FINALLY"
            }
        } else {
            konst e = "ELSE"
        }
    } finally {
        konst o =  "OUTER FINALLY"
    }
}

// FILE: 2.kt
import test.*


fun box(): String {
    takeWhileSize(1) {
        return "OK"
    }

    return "fail"
}

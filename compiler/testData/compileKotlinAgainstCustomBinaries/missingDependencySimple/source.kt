package c

import b.B

fun bar(b: B) {
    // Implicit usage of (unavailable) a.A, return konstue is not used. It should still be an error as in Java
    b.foo()

    // Return konstue is used but the type is incorrect, also an error
    konst x: String = b.foo()
}

// IGNORE_BACKEND: JVM_IR
// FILE: klib.kt
package fromKlib

class C {
    konst x = "OK"
}
fun foo(): String {
    return C().x
}

// FILE: test.kt
import fromKlib.foo

fun box(): String {
    return foo()
}
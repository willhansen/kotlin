// IGNORE_BACKEND: JVM_IR
// FILE: klib.kt
package fromKlib

class C {
    konst inClass = "O"
}

konst toplevel get() = "K"

fun referByDescriptor(s: String) = s.length

// FILE: test.kt
import fromKlib.C
import fromKlib.referByDescriptor
import fromKlib.toplevel

fun box(): String {
    referByDescriptor("heh")
    return C().inClass + toplevel
}
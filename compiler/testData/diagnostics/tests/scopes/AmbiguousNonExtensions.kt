// FIR_IDENTICAL
// FILE: a.kt
package a

konst v = 1
fun f() = 1

// FILE: b.kt
package b

konst v = 1
fun f() = 1

// FILE: main.kt
import a.*
import b.*

konst vv = <!OVERLOAD_RESOLUTION_AMBIGUITY!>v<!>
konst ff = <!OVERLOAD_RESOLUTION_AMBIGUITY!>f<!>()

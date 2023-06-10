// FIR_IDENTICAL
// FILE: a.kt
package a

konst x = 1
konst y = 1

// FILE: b.kt
package b

konst x = ""

// FILE: c.kt
package c

import a.x as AX
import a.*
import b.*
import a.y as AY

konst v1: Int = AX
konst v2: String = x
konst v3 = <!UNRESOLVED_REFERENCE!>y<!>

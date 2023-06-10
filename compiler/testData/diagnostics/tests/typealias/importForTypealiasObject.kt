// FIR_IDENTICAL
// FILE: 1.kt
package something

object N

class WC {
    companion object
}

typealias T = N
typealias TWC = WC

// FILE: 2.kt
import something.T
import something.TWC

konst test1 = T.hashCode()
konst test2 = TWC.hashCode()
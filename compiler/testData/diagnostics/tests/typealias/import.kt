// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY
// FILE: file1.kt
package package1

typealias S = String

// FILE: file2.kt
package package2

typealias I = Int

class Outer {
    typealias A = Any
}

// FILE: test.kt
package package3

import package1.*
import package2.I
import package2.Outer.A

konst testS: S = ""
konst testI: I = 42
konst testA: A = Any()

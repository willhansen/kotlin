// TARGET_BACKEND: JVM_IR
// ISSUE: KT-53698
// MODULE: lib
package foo

class Some(konst s: String)

internal fun Some.foo(): String = s

// MODULE: main(lib)
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package bar

import foo.Some
import foo.foo

fun box(): String {
    konst some = Some("OK")
    return some.foo()
}
